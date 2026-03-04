package com.khoi.game_library.dto.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SteamOwnedGamesResponse(
        @JsonProperty("response") Response response
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
            @JsonProperty("game_count") int gameCount,
            @JsonProperty("games") List<SteamGame> games
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SteamGame(
            @JsonProperty("appid") Long appId,
            @JsonProperty("name") String name,
            @JsonProperty("playtime_forever") Integer playtimeForever,
            @JsonProperty("img_icon_url") String imgIconUrl
    ) {}
}