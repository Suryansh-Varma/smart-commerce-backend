package com.ansh.smart_commerce.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ansh.smart_commerce.entity.User;
public interface UserRepository extends JpaRepository<User, Long> {
    java.util.Optional<User> findByEmail(String email);
}
