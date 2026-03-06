package com.khoi.game_library.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
@Getter @Setter @NoArgsConstructor
public class Game {
    @Id
    private Long appId;

    @Column(nullable = false)
    private String title;

    private String coverArtUrl;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Achievement> achievements = new ArrayList<>();
}
