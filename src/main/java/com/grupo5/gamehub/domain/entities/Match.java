package com.grupo5.gamehub.domain.entities;

import com.grupo5.gamehub.domain.enums.MatchStatus; // Nueva importación
import com.grupo5.gamehub.domain.enums.Result;
import jakarta.persistence.*;
import lombok.*;

import java.util.List; // Aunque no se usa directamente en este constructor, lo dejo si lo usas en el futuro

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player1_id", nullable = false)
    private User player1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player2_id", nullable = false)
    private User player2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Result result;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    @Column(nullable = false)
    private Integer roundNumber;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages;

    public Match(Tournament tournament, User player1, User player2, Integer roundNumber) {
        this.tournament = tournament;
        this.player1 = player1;
        this.player2 = player2;
        this.roundNumber = roundNumber;
        this.status = MatchStatus.PENDING;
        this.result = Result.PENDING;

    }
}