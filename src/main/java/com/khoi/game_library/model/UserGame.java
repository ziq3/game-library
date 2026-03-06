package com.khoi.game_library.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_games",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_steam_id", "app_id"}))
@Getter @Setter @NoArgsConstructor
public class UserGame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_steam_id", referencedColumnName = "steam_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", nullable = false)
    private Game game;

    private Integer playtimeForever; // in minutes, from Steam API
}