package com.redizego.redi_ze_go.repositories;

import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.Rating;
import com.redizego.redi_ze_go.entities.Ride;
import com.redizego.redi_ze_go.entities.Rider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByRider(Rider rider);

    List<Rating> findByDriver(Driver driver);

    Optional<Rating> findByRide(Ride ride);
}
