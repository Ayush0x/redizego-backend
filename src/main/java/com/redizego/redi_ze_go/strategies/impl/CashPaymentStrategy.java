package com.redizego.redi_ze_go.strategies.impl;

import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.Payment;
import com.redizego.redi_ze_go.entities.enums.PaymentStatus;
import com.redizego.redi_ze_go.entities.enums.TransactionMethods;
import com.redizego.redi_ze_go.repositories.PaymentRepository;
import com.redizego.redi_ze_go.services.WalletService;
import com.redizego.redi_ze_go.strategies.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CashPaymentStrategy implements PaymentStrategy {

    private final WalletService walletService;
    private final PaymentRepository paymentRepository;
    @Override
    @Transactional
    public void processPayment(Payment payment) {
        Driver driver=payment.getRide().getDriver();

        double plaformCommission=payment.getAmount()*PLATFORM_FEE;

        walletService.deductMoneyFromWallet(driver.getUser(),plaformCommission,null,payment.getRide(), TransactionMethods.RIDE);

        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);
    }
}
