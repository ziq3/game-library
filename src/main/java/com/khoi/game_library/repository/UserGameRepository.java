package com.khoi.game_library.repository;

import com.khoi.game_library.model.UserGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserGameRepository extends JpaRepository<UserGame, Long> {
    List<UserGame> findByUserId(UUID userId);
    Optional<UserGame> findByUserIdAndGameAppId(UUID userId, Long appId);
    boolean existsByUserIdAndGameAppId(UUID userId, Long appId);
}
