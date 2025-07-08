package com.redizego.redi_ze_go.services;

import com.redizego.redi_ze_go.entities.Payment;
import com.redizego.redi_ze_go.entities.Ride;
import com.redizego.redi_ze_go.entities.enums.PaymentStatus;

public interface PaymentService {

    void processPayment(Ride ride);

    Payment createNewPayment(Ride ride);

    void updatePaymentStatus(Payment payment, PaymentStatus paymentStatus);
}
