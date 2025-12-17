package project.cloudstorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.cloudstorage.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
