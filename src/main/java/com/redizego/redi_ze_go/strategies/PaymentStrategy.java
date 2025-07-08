package com.redizego.redi_ze_go.strategies;

import com.redizego.redi_ze_go.entities.Payment;

public interface PaymentStrategy {

    Double PLATFORM_FEE=0.25;

    void processPayment(Payment payment);
}
