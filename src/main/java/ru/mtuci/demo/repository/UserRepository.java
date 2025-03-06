package ru.mtuci.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mtuci.demo.model.ApplicationUser;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<ApplicationUser, UUID> {

    Optional<ApplicationUser> findById(UUID Id);
    Optional<ApplicationUser> findByUsername(String username);
    Optional<ApplicationUser> findByEmail(String email);

}