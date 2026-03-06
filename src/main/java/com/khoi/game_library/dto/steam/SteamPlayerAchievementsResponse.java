package com.khoi.game_library.dto.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SteamPlayerAchievementsResponse(
        @JsonProperty("playerstats") PlayerStats playerStats
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerStats(
            @JsonProperty("steamID") String steamId,
            @JsonProperty("gameName") String gameName,
            @JsonProperty("success") Boolean success,
            @JsonProperty("achievements") List<SteamAchievement> achievements
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SteamAchievement(
            @JsonProperty("apiname") String apiName,
            @JsonProperty("achieved") Integer achieved,
            @JsonProperty("unlocktime") Long unlockTime,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description
    ) {}
}
