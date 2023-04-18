package com.skcraft.launcher.auth.elyby.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElyByAccountError {
	private String name;
	private int status;
	private String message;
}
