package com.khoi.game_library.repository;

import com.khoi.game_library.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByGameAppId(Long appId);
    Optional<Achievement> findByApiNameAndGameAppId(String apiName, Long appId);
}
