package com.khoi.game_library.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {

    @Id
    @Column(name = "steam_id", nullable = false, unique = true)
    private String steamId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserGame> library = new HashSet<>();
}