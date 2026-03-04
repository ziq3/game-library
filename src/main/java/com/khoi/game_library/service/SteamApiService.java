package com.khoi.game_library.service;

import com.khoi.game_library.dto.steam.SteamOwnedGamesResponse;
import com.khoi.game_library.exception.SteamProfilePrivateException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SteamApiService {

    private final RestClient steamRestClient;
    private final String apiKey;

    public SteamApiService(RestClient steamRestClient, @Value("${steam.api.key}") String apiKey) {
        this.steamRestClient = steamRestClient;
        this.apiKey = apiKey;
    }

    public SteamOwnedGamesResponse getOwnedGames(String steamId) {
        SteamOwnedGamesResponse response = steamRestClient.get()
                .uri("/IPlayerService/GetOwnedGames/v0001/?key={key}&steamid={steamId}&include_appinfo=true&include_played_free_games=true&format=json",
                        apiKey, steamId)
                .retrieve()
                .body(SteamOwnedGamesResponse.class);

        if (response == null || response.response() == null || response.response().games() == null) {
            throw new SteamProfilePrivateException(steamId);
        }

        return response;
    }
}