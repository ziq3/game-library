package com.khoi.game_library.service;

import com.khoi.game_library.dto.steam.SteamOwnedGamesResponse;
import com.khoi.game_library.dto.steam.SteamPlayerAchievementsResponse;
import com.khoi.game_library.model.Achievement;
import com.khoi.game_library.model.Game;
import com.khoi.game_library.model.User;
import com.khoi.game_library.model.UserAchievement;
import com.khoi.game_library.model.UserGame;
import com.khoi.game_library.repository.AchievementRepository;
import com.khoi.game_library.repository.GameRepository;
import com.khoi.game_library.repository.UserAchievementRepository;
import com.khoi.game_library.repository.UserGameRepository;
import com.khoi.game_library.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

@Service
public class SyncService {

    private final SteamApiService steamApiService;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final UserGameRepository userGameRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;

    public SyncService(SteamApiService steamApiService,
                       UserRepository userRepository,
                       GameRepository gameRepository,
                       UserGameRepository userGameRepository,
                       AchievementRepository achievementRepository,
                       UserAchievementRepository userAchievementRepository) {
        this.steamApiService = steamApiService;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.userGameRepository = userGameRepository;
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
    }

    @Transactional
    public int syncLibrary(String steamId) {
        String safeSteamId = Objects.requireNonNull(steamId, "steamId must not be null").trim();
        if (safeSteamId.isBlank()) {
            throw new IllegalArgumentException("steamId must not be blank");
        }

        User user = userRepository.findById(safeSteamId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setSteamId(safeSteamId);
                    return userRepository.save(newUser);
                });

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
            if (!userGameRepository.existsByUserSteamIdAndGameAppId(safeSteamId, appId)) {
                UserGame userGame = new UserGame();
                userGame.setUser(user);
                userGame.setGame(game);
                userGame.setPlaytimeForever(steamGame.playtimeForever());
                userGameRepository.save(userGame);
                newGamesAdded++;
            } else {
                // Update playtime for existing entries
                userGameRepository.findByUserSteamIdAndGameAppId(safeSteamId, appId)
                        .ifPresent(existing -> {
                            existing.setPlaytimeForever(steamGame.playtimeForever());
                            userGameRepository.save(existing);
                        });
            }

            // Achievement sync is expensive; skip untouched games to avoid very long sync runs.
            if (steamGame.playtimeForever() != null && steamGame.playtimeForever() > 0) {
                syncAchievementsForGame(user, game, safeSteamId, appId);
            }
        }

        return newGamesAdded;
    }

    private void syncAchievementsForGame(User user, Game game, String steamId, Long appId) {
        List<SteamPlayerAchievementsResponse.SteamAchievement> steamAchievements;
        try {
            SteamPlayerAchievementsResponse achievementResponse = steamApiService.getPlayerAchievements(steamId, appId);
            steamAchievements = achievementResponse != null
                    && achievementResponse.playerStats() != null
                    && achievementResponse.playerStats().achievements() != null
                    ? achievementResponse.playerStats().achievements()
                    : List.of();
        } catch (RestClientException ex) {
            // Some games do not expose achievements; skip them without failing full sync.
            return;
        }

        for (SteamPlayerAchievementsResponse.SteamAchievement steamAchievement : steamAchievements) {
            String apiName = normalizeApiName(steamAchievement.apiName());
            if (apiName.isBlank()) {
                continue;
            }

            Achievement achievement = achievementRepository
                    .findByApiNameAndGameAppId(apiName, appId)
                    .orElseGet(() -> {
                        Achievement newAchievement = new Achievement();
                        newAchievement.setApiName(apiName);
                        newAchievement.setGame(game);
                        String displayName = Objects.requireNonNullElse(steamAchievement.name(), "").trim();
                        newAchievement.setDisplayName(displayName.isBlank() ? apiName : displayName);
                        newAchievement.setDescription(steamAchievement.description());
                        return achievementRepository.save(newAchievement);
                    });

            boolean achievementChanged = false;
            if (achievement.getDisplayName() == null || achievement.getDisplayName().isBlank()) {
                String displayName = Objects.requireNonNullElse(steamAchievement.name(), "").trim();
                achievement.setDisplayName(displayName.isBlank() ? apiName : displayName);
                achievementChanged = true;
            }
            if (achievement.getDescription() == null || achievement.getDescription().isBlank()) {
                achievement.setDescription(steamAchievement.description());
                achievementChanged = true;
            }
            if (achievementChanged) {
                achievementRepository.save(achievement);
            }

            UserAchievement userAchievement = userAchievementRepository
                    .findByUserSteamIdAndAchievementId(steamId, achievement.getId())
                    .orElseGet(() -> {
                        UserAchievement newUserAchievement = new UserAchievement();
                        newUserAchievement.setUser(user);
                        newUserAchievement.setAchievement(achievement);
                        return newUserAchievement;
                    });

            boolean unlocked = steamAchievement.achieved() != null && steamAchievement.achieved() == 1;
            LocalDateTime unlockedAt = unlocked ? toUtcDateTime(steamAchievement.unlockTime()) : null;

            boolean userAchievementChanged = !Objects.equals(userAchievement.getUnlocked(), unlocked)
                    || !Objects.equals(userAchievement.getUnlockedAt(), unlockedAt);

            if (userAchievementChanged) {
                userAchievement.setUnlocked(unlocked);
                userAchievement.setUnlockedAt(unlockedAt);
                userAchievementRepository.save(userAchievement);
            }
        }
    }

    private String normalizeApiName(String apiName) {
        return Objects.requireNonNullElse(apiName, "").trim().toLowerCase();
    }

    private LocalDateTime toUtcDateTime(Long unlockTime) {
        if (unlockTime == null || unlockTime <= 0) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(unlockTime), ZoneOffset.UTC);
    }
}