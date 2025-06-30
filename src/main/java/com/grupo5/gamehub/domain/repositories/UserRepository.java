package com.grupo5.gamehub.domain.repositories;

import aj.org.objectweb.asm.commons.Remapper;
import com.grupo5.gamehub.domain.entities.User;
import com.grupo5.gamehub.domain.enums.Role;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
  Optional<User> findByEmail(String email);
  List<User> findByRole(Role role);

  Remapper findById(SingularAttribute<AbstractPersistable, Serializable> id);
}