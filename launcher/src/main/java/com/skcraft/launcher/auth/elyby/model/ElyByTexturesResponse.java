package com.skcraft.launcher.auth.elyby.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElyByTexturesResponse {
    @JsonProperty("SKIN") private Texture skin;
    @JsonProperty("CAPE") private Texture cape;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Texture {
        private String url;
    }
}
