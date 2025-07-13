package com.redizego.redi_ze_go.repositories;

import com.redizego.redi_ze_go.entities.Rider;
import com.redizego.redi_ze_go.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiderRepository extends JpaRepository<Rider,Long> {
    Optional<Rider> findByUser(User user);
}
