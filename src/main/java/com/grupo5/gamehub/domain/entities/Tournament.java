package com.grupo5.gamehub.domain.entities;

import com.grupo5.gamehub.domain.enums.TournamentStatus;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Entity;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "Tournaments")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentStatus status; // Estado del torneo: CREADO, EN_PROGRESO, FINALIZADO

    @Column(nullable = false)
    private Integer maxPlayers; // Capacidad máxima de jugadores

    @Column(nullable = false)
    private LocalDateTime createdAt; // Fecha y hora de creación del torneo

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator; // El usuario (ADMIN) que creó el torneo

    @ManyToMany
    @JoinTable(
            name = "tournament_players",
            joinColumns = @JoinColumn(name = "tournament_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private List<User> players = new ArrayList<>();

    public Tournament(String name, TournamentStatus status, Integer maxPlayers, LocalDateTime createdAt, User creator) {
        this.name = name;
        this.status = status;
        this.maxPlayers = maxPlayers;
        this.createdAt = createdAt;
        this.creator = creator;
    }
}