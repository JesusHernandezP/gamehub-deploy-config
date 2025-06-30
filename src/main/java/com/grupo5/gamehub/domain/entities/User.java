package com.grupo5.gamehub.domain.entities;

import com.grupo5.gamehub.domain.enums.Role;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "Users")
@EqualsAndHashCode(of = {"id"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role ;

    private Integer rank;
    private Integer points ;

    @ManyToMany(mappedBy = "players")
    private List<Tournament> tournaments = new ArrayList<>();

    @OneToMany(mappedBy = "sender")
    private List<Message> messagesSent = new ArrayList<>();
}