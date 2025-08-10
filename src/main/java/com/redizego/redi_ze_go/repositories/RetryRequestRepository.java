package com.redizego.redi_ze_go.repositories;

import com.redizego.redi_ze_go.entities.RetryRequest;
import com.redizego.redi_ze_go.entities.enums.RetryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetryRequestRepository extends JpaRepository<RetryRequest, Long> {
    List<RetryRequest> findByRetryStatus(RetryStatus status);
}
