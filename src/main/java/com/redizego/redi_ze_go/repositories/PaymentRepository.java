package com.redizego.redi_ze_go.repositories;

import com.redizego.redi_ze_go.entities.Payment;
import com.redizego.redi_ze_go.entities.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRide(Ride ride);
}
