package com.redizego.redi_ze_go.repositories;

import com.redizego.redi_ze_go.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
}
