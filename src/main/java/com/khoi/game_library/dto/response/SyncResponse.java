package com.khoi.game_library.dto.response;

public record SyncResponse(
        String message,
        int totalGamesInLibrary,
        int newGamesAdded
) {}