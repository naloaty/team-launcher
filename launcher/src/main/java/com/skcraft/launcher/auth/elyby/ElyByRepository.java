package com.skcraft.launcher.auth.elyby;

import com.skcraft.launcher.auth.AuthenticationException;
import com.skcraft.launcher.auth.elyby.model.ElyByAccountError;
import com.skcraft.launcher.auth.elyby.model.ElyByAccountResponse;
import com.skcraft.launcher.auth.elyby.model.ElyByAuthResponse;
import com.skcraft.launcher.auth.elyby.model.ElyByTexturesResponse;
import com.skcraft.launcher.util.HttpRequest;
import com.skcraft.launcher.util.SharedLocale;

import java.io.IOException;
import java.net.URL;

import static com.skcraft.launcher.util.HttpRequest.url;

public class ElyByRepository {
    private static final URL ELY_ACCOUNT = url("https://account.ely.by/api/account/v1/info");

    public static ElyByAccountResponse getUserAccount(ElyByAuthResponse auth)
            throws IOException, InterruptedException, AuthenticationException {
        return HttpRequest.get(ELY_ACCOUNT)
                .header("Authorization", auth.getAuthorization())
                .execute()
                .expectResponseCodeOr(200, req -> {
                    HttpRequest.BufferedResponse content = req.returnContent();

                    if (content.asBytes().length == 0) {
                        return new AuthenticationException("Got empty response from Ely.by service",
                                SharedLocale.tr("elyby.login.error", req.getResponseCode()));
                    }

                    ElyByAccountError error = content.asJson(ElyByAccountError.class);

                    if (error.getStatus() == 403) {
                        return new AuthenticationException("Session expired", true);
                    }

                    return new AuthenticationException(error.getMessage(),
                            SharedLocale.tr("login.elyby.error", error.getMessage(), error.getStatus()));
                })
                .returnContent()
                .asJson(ElyByAccountResponse.class);
    }

    public static ElyByTexturesResponse getUserTextures(String nickname)
            throws IOException, InterruptedException, RequestException {

        return HttpRequest.get(url("http://skinsystem.ely.by/textures/" + nickname))
                .execute()
                .expectResponseCodeOr(200, req -> {
                    if (req.getResponseCode() == 204) {
                        return new RequestException("Textures for given nickname not found",
                                SharedLocale.tr("elyby.texturesNotFound", nickname));
                    }

                    return new RequestException("An error occurred while fetching user textures",
                            SharedLocale.tr("elyby.error", req.getResponseCode()));
                })
                .returnContent()
                .asJson(ElyByTexturesResponse.class);
    }
}
