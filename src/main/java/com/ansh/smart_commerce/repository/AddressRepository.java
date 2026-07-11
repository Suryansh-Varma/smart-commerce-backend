package com.ansh.smart_commerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ansh.smart_commerce.entity.Address;
import com.ansh.smart_commerce.entity.User;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUser(User user);

    Optional<Address> findByUserAndIsDefaultTrue(User user);
}
