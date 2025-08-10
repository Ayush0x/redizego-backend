package com.redizego.redi_ze_go.repositories;

import com.redizego.redi_ze_go.entities.User;
import com.redizego.redi_ze_go.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUser(User user);
}
