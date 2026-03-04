package com.khoi.game_library.repository;

import com.khoi.game_library.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUserIdAndAchievementGameAppId(UUID userId, Long appId);
    Optional<UserAchievement> findByUserIdAndAchievementId(UUID userId, Long achievementId);
    boolean existsByUserIdAndAchievementId(UUID userId, Long achievementId);
}
