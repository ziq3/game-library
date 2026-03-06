package com.khoi.game_library.repository;

import com.khoi.game_library.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUserSteamIdAndAchievementGameAppId(String steamId, Long appId);
    boolean existsByUserSteamIdAndAchievementGameAppId(String steamId, Long appId);
    Optional<UserAchievement> findByUserSteamIdAndAchievementId(String steamId, Long achievementId);
    boolean existsByUserSteamIdAndAchievementId(String steamId, Long achievementId);
}
