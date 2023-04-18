package com.skcraft.launcher.auth.elyby.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElyByAuthResponse {
	private String tokenType;
	private String accessToken;
	private String refreshToken;
	private int expiresIn;

	@JsonIgnore
	public String getAuthorization() {
		return String.format("%s %s", tokenType, accessToken);
	}
}
