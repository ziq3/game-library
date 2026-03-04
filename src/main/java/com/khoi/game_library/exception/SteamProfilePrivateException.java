package com.khoi.game_library.exception;

public class SteamProfilePrivateException extends RuntimeException {
    public SteamProfilePrivateException(String steamId) {
        super("Steam profile is private or invalid for Steam ID: " + steamId);
    }
}