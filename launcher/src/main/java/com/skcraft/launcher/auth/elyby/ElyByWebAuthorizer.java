package com.skcraft.launcher.auth.elyby;

import com.skcraft.launcher.auth.AuthenticationException;
import com.skcraft.launcher.auth.microsoft.OauthHttpHandler;
import com.skcraft.launcher.auth.microsoft.OauthResult;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.HttpRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Handles the Ely.by leg of OAuth authorization.
 */
@RequiredArgsConstructor
public class ElyByWebAuthorizer {
	private final String clientId;
	@Getter private String redirectUri;

	public OauthResult authorize() throws IOException, AuthenticationException, InterruptedException {
		if (Desktop.isDesktopSupported()) {
			// Interactive auth
			return authorizeInteractive();
		} else {
			// TODO Device code auth
			return null;
		}
	}

	private OauthResult authorizeInteractive() throws IOException, AuthenticationException, InterruptedException {
		OauthHttpHandler httpHandler = new OauthHttpHandler();
		SwingHelper.openURL(generateInteractiveUrl(httpHandler.getPort()));

		return httpHandler.await();
	}

	private URI generateInteractiveUrl(int port) throws AuthenticationException {
		redirectUri = "http://localhost:" + port;

		URI interactive;
		try {
			HttpRequest.Form query = HttpRequest.Form.form();
			query.add("client_id", clientId);
			query.add("scope", "minecraft_server_session offline_access account_info");
			query.add("response_type", "code");
			query.add("redirect_uri", redirectUri);
			query.add("prompt", "select_account");

			interactive = new URI("https://account.ely.by/oauth2/v1?"
					+ query.toString());
		} catch (URISyntaxException e) {
			throw new AuthenticationException(e, "Failed to generate OAuth URL");
		}

		return interactive;
	}
}
