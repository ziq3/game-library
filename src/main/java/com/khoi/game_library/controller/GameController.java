package com.khoi.game_library.controller;

import com.khoi.game_library.dto.response.GameResponse;
import com.khoi.game_library.dto.response.SyncResponse;
import com.khoi.game_library.model.User;
import com.khoi.game_library.model.UserGame;
import com.khoi.game_library.repository.UserGameRepository;
import com.khoi.game_library.repository.UserRepository;
import com.khoi.game_library.service.SyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<SyncResponse> syncLibrary(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow();

        int newGames = syncService.syncLibrary(user.getId());
        int totalGames = userGameRepository.findByUserId(user.getId()).size();

        return ResponseEntity.ok(new SyncResponse(
                "Steam library synced successfully",
                totalGames,
                newGames
        ));
    }

    @GetMapping
    public ResponseEntity<List<GameResponse>> getLibrary(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow();

        List<GameResponse> games = userGameRepository.findByUserId(user.getId()).stream()
                .map(this::toGameResponse)
                .toList();

        return ResponseEntity.ok(games);
    }

    @PutMapping("/steam-id")
    public ResponseEntity<Map<String, String>> setSteamId(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow();

        String steamId = body.get("steamId");
        if (steamId == null || steamId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "steamId is required"));
        }

        user.setSteamId(steamId);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Steam ID updated successfully",
                "steamId", steamId
        ));
    }

    private GameResponse toGameResponse(UserGame userGame) {
        return new GameResponse(
                userGame.getGame().getAppId(),
                userGame.getGame().getTitle(),
                userGame.getGame().getDeveloper(),
                userGame.getGame().getGenre(),
                userGame.getGame().getCoverArtUrl(),
                userGame.getPlaytimeForever()
        );
    }
}