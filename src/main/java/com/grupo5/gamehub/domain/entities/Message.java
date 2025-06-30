package com.grupo5.gamehub.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    // Constructor para mensajes de torneo
    public Message(User sender, String content, Tournament tournament) {
        this.sender = sender;
        this.content = content;
        this.tournament = tournament;
        this.match = null;
    }

    // Constructor para mensajes de partida
    public Message(User sender, String content, Match match) {
        this.sender = sender;
        this.content = content;
        this.match = match;
        this.tournament = null;
    }

    @PrePersist
    @PreUpdate
    private void validateAssociations() {
        if (tournament != null && match != null) {
            throw new IllegalStateException("Un mensaje no puede estar asociado tanto a un torneo como a una partida.");
        }
        if (tournament == null && match == null) {
            throw new IllegalStateException("Un mensaje debe estar asociado a un torneo o a una partida.");
        }
    }
}