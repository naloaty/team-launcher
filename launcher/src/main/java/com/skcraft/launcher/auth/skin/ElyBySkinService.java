package com.skcraft.launcher.auth.skin;

import com.skcraft.launcher.auth.elyby.model.ElyByTexturesResponse;
import com.skcraft.launcher.util.HttpRequest;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.logging.Level;

@Log
public class ElyBySkinService {
	static byte[] downloadSkin(String textureUrl) throws IOException, InterruptedException {
		return HttpRequest.get(HttpRequest.url(textureUrl))
				.execute()
				.expectResponseCode(200)
				.returnContent()
				.asBytes();
	}

	public static byte[] fetchSkinHead(ElyByTexturesResponse textures) throws InterruptedException {
		try {
			byte[] skin = downloadSkin(textures.getSkin().getUrl());

			return SkinProcessor.renderHead(skin);
		} catch (IOException e) {
			log.log(Level.WARNING, "Failed to download or process skin from Ely.by.", e);
			return null;
		}
	}
}
