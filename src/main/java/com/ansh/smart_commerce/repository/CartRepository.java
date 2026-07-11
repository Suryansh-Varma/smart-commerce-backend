package com.ansh.smart_commerce.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ansh.smart_commerce.entity.CartItem;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.entity.User;
import java.util.List;
import java.util.Optional;
public interface CartRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProduct(User user,Product product);
}
