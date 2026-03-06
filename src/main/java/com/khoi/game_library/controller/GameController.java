package com.khoi.game_library.controller;

import com.khoi.game_library.dto.response.GameResponse;
import com.khoi.game_library.dto.response.SyncResponse;
import com.khoi.game_library.model.User;
import com.khoi.game_library.model.UserGame;
import com.khoi.game_library.repository.UserGameRepository;
import com.khoi.game_library.repository.UserRepository;
import com.khoi.game_library.service.SyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final SyncService syncService;
    private final UserRepository userRepository;
    private final UserGameRepository userGameRepository;

    public GameController(SyncService syncService,
                          UserRepository userRepository,
                          UserGameRepository userGameRepository) {
        this.syncService = syncService;
        this.userRepository = userRepository;
        this.userGameRepository = userGameRepository;
    }

    @PostMapping("/sync")
    public ResponseEntity<SyncResponse> syncLibrary(@RequestParam String steamId) {
        int newGames = syncService.syncLibrary(steamId);
        int totalGames = userGameRepository.findByUserSteamId(steamId).size();

        return ResponseEntity.ok(new SyncResponse(
                "Steam library synced successfully",
                totalGames,
                newGames
        ));
    }

    @GetMapping
    public ResponseEntity<List<GameResponse>> getLibrary(@RequestParam String steamId) {
        List<GameResponse> games = userGameRepository.findByUserSteamId(steamId).stream()
                .map(this::toGameResponse)
                .toList();

        return ResponseEntity.ok(games);
    }

    private GameResponse toGameResponse(UserGame userGame) {
        return new GameResponse(
                userGame.getGame().getAppId(),
                userGame.getGame().getTitle(),
                userGame.getGame().getCoverArtUrl(),
                userGame.getPlaytimeForever()
        );
    }
}