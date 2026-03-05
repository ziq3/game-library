package com.khoi.game_library.service;

import com.khoi.game_library.dto.steam.SteamOwnedGamesResponse;
import com.khoi.game_library.exception.ResourceNotFoundException;
import com.khoi.game_library.model.Game;
import com.khoi.game_library.model.User;
import com.khoi.game_library.model.UserGame;
import com.khoi.game_library.repository.GameRepository;
import com.khoi.game_library.repository.UserGameRepository;
import com.khoi.game_library.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class SyncService {

    private final SteamApiService steamApiService;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final UserGameRepository userGameRepository;

    public SyncService(SteamApiService steamApiService,
                       UserRepository userRepository,
                       GameRepository gameRepository,
                       UserGameRepository userGameRepository) {
        this.steamApiService = steamApiService;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.userGameRepository = userGameRepository;
    }

    @Transactional
    public int syncLibrary(UUID userId) {
        UUID safeUserId = Objects.requireNonNull(userId, "userId must not be null");

        User user = userRepository.findById(safeUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (user.getSteamId() == null || user.getSteamId().isBlank()) {
            throw new IllegalStateException("User has no Steam ID linked. Please set your Steam ID first.");
        }

        SteamOwnedGamesResponse response = steamApiService.getOwnedGames(user.getSteamId());

        int newGamesAdded = 0;

        for (SteamOwnedGamesResponse.SteamGame steamGame : response.response().games()) {
            Long appId = Objects.requireNonNull(steamGame.appId(), "Steam appId must not be null");

            // Upsert the Game entity (create if it doesn't exist)
            Game game = gameRepository.findById(appId)
                    .orElseGet(() -> {
                        Game newGame = new Game();
                        newGame.setAppId(appId);
                        newGame.setTitle(steamGame.name());
                        // Build Steam header image URL as default cover art
                        newGame.setCoverArtUrl(
                                "https://cdn.akamai.steamstatic.com/steam/apps/" + appId + "/header.jpg"
                        );
                        return gameRepository.save(newGame);
                    });

            // Create UserGame link if it doesn't already exist
            if (!userGameRepository.existsByUserIdAndGameAppId(safeUserId, appId)) {
                UserGame userGame = new UserGame();
                userGame.setUser(user);
                userGame.setGame(game);
                userGame.setPlaytimeForever(steamGame.playtimeForever());
                userGameRepository.save(userGame);
                newGamesAdded++;
            } else {
                // Update playtime for existing entries
                userGameRepository.findByUserIdAndGameAppId(safeUserId, appId)
                        .ifPresent(existing -> {
                            existing.setPlaytimeForever(steamGame.playtimeForever());
                            userGameRepository.save(existing);
                        });
            }
        }

        return newGamesAdded;
    }
}