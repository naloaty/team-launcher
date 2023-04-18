package com.skcraft.launcher.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.skcraft.launcher.auth.elyby.ElyByRepository;
import com.skcraft.launcher.auth.elyby.ElyByWebAuthorizer;
import com.skcraft.launcher.auth.elyby.RequestException;
import com.skcraft.launcher.auth.elyby.model.ElyByAccountResponse;
import com.skcraft.launcher.auth.elyby.model.ElyByAuthResponse;
import com.skcraft.launcher.auth.elyby.model.ElyByTexturesResponse;
import com.skcraft.launcher.auth.microsoft.OauthResult;
import com.skcraft.launcher.auth.skin.ElyBySkinService;
import com.skcraft.launcher.util.HttpRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import static com.skcraft.launcher.util.HttpRequest.url;

@RequiredArgsConstructor
public class ElyByLoginService implements LoginService {
	private static final URL ELY_TOKEN_URL = url("https://account.ely.by/api/oauth2/v1/token");

	private final String clientId;

	/**
	 * Trigger a full login sequence with the Ely.by authenticator.
	 *
	 * @param oauthDone Callback called when OAuth is complete and automatic login is about to begin.
	 * @return Valid {@link Session} instance representing the logged-in player.
	 * @throws IOException if any I/O error occurs.
	 * @throws InterruptedException if the current thread is interrupted
	 * @throws AuthenticationException if authentication fails in any way, this is thrown with a human-useful message.
	 */
	public Session login(Receiver oauthDone) throws IOException, InterruptedException, AuthenticationException {
		ElyByWebAuthorizer authorizer = new ElyByWebAuthorizer(clientId);
		OauthResult auth = authorizer.authorize();

		if (auth.isError()) {
			OauthResult.Error error = (OauthResult.Error) auth;
			throw new AuthenticationException(error.getErrorMessage());
		}

		ElyByAuthResponse response = exchangeToken(form -> {
			form.add("grant_type", "authorization_code");
			form.add("redirect_uri", authorizer.getRedirectUri());
			form.add("code", ((OauthResult.Success) auth).getAuthCode());
		});

		oauthDone.tell();
		Profile session = loadProfile(response, null);
		session.setRefreshToken(response.getRefreshToken());

		return session;
	}

	@Override
	public Session restore(SavedSession savedSession)
			throws IOException, InterruptedException, AuthenticationException {
		ElyByAuthResponse response = exchangeToken(form -> {
			form.add("grant_type", "refresh_token");
			form.add("refresh_token", savedSession.getRefreshToken());
		});

		Profile session = loadProfile(response, savedSession);
		session.setRefreshToken(response.getRefreshToken());

		return session;
	}

	private ElyByAuthResponse exchangeToken(Consumer<HttpRequest.Form> formConsumer)
			throws IOException, InterruptedException, AuthenticationException {
		HttpRequest.Form form = HttpRequest.Form.form();
		form.add("client_id", clientId);
		formConsumer.accept(form);

		return HttpRequest.post(ELY_TOKEN_URL)
				.bodyForm(form)
				.execute()
				.expectResponseCodeOr(200, (req) -> {
					TokenError error = req.returnContent().asJson(TokenError.class);

					return new AuthenticationException(error.errorDescription);
				})
				.returnContent()
				.asJson(ElyByAuthResponse.class);
	}

	private Profile loadProfile(ElyByAuthResponse auth, SavedSession previous)
			throws IOException, InterruptedException, AuthenticationException {

		ElyByAccountResponse account = ElyByRepository.getUserAccount(auth);
		Profile session = new Profile(auth, account);

		if (previous != null && previous.getAvatarImage() != null) {
			session.setAvatarImage(previous.getAvatarImage());
		} else {
			try {
				ElyByTexturesResponse textures = ElyByRepository.getUserTextures(account.getNickname());
				session.setAvatarImage(ElyBySkinService.fetchSkinHead(textures));
			} catch (RequestException e) {
				session.setAvatarImage(null);
			}
		}

		return session;
	}

	@Data
	public static class Profile implements Session {
		private final ElyByAuthResponse auth;
		private final ElyByAccountResponse account;
		private final Map<String, String> userProperties = Collections.emptyMap();
		private String refreshToken;
		private byte[] avatarImage;

		@Override
		public String getUuid() {
			return account.getUuid();
		}

		@Override
		public String getName() {
			return account.getNickname();
		}

		@Override
		public String getAccessToken() {
			return auth.getAccessToken();
		}

		@Override
		public String getSessionToken() {
			return String.format("token:%s:%s", getAccessToken(), getUuid());
		}

		@Override
		public UserType getUserType() {
			return UserType.ELYBY;
		}

		@Override
		public boolean isOnline() {
			return true;
		}

		@Override
		public SavedSession toSavedSession() {
			SavedSession savedSession = new SavedSession();

			savedSession.setType(getUserType());
			savedSession.setUsername(getName());
			savedSession.setUuid(getUuid());
			savedSession.setAccessToken(getAccessToken());
			savedSession.setRefreshToken(getRefreshToken());
			savedSession.setAvatarImage(getAvatarImage());

			return savedSession;
		}
	}

	@Data
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class TokenError {
		private String error;
		private String errorDescription;
	}

	@FunctionalInterface
	public interface Receiver {
		void tell();
	}
}
