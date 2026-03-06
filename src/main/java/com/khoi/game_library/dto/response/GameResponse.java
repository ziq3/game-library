package com.khoi.game_library.dto.response;

public record GameResponse(
        Long appId,
        String title,
        String coverArtUrl,
        Integer playtimeForever
) {}