package com.khoi.game_library.repository;

import com.khoi.game_library.model.UserGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserGameRepository extends JpaRepository<UserGame, Long> {
    List<UserGame> findByUserSteamId(String steamId);
    Optional<UserGame> findByUserSteamIdAndGameAppId(String steamId, Long appId);
    boolean existsByUserSteamIdAndGameAppId(String steamId, Long appId);
}
