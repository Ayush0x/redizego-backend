package com.redizego.redi_ze_go.strategies.impl;

import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.Payment;
import com.redizego.redi_ze_go.entities.enums.PaymentStatus;
import com.redizego.redi_ze_go.entities.enums.TransactionMethods;
import com.redizego.redi_ze_go.repositories.PaymentRepository;
import com.redizego.redi_ze_go.services.WalletService;
import com.redizego.redi_ze_go.strategies.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Payment strategy implementation for cash-based ride payments.
 * 
 * This strategy handles the unique aspects of cash payments where the rider
 * pays the driver directly in cash at the end of the ride. The platform still
 * needs to collect its commission from the driver's wallet, since the driver
 * receives the full fare amount in cash.
 * 
 * Payment Flow:
 * 1. Rider pays full fare amount to driver in cash
 * 2. Driver keeps rider's payment (already received)
 * 3. Platform deducts commission from driver's wallet
 * 4. Payment status is marked as completed
 * 
 * Key Characteristics:
 * - No upfront payment validation required (unlike wallet payments)
 * - Driver receives immediate payment from rider
 * - Platform commission is collected via wallet deduction
 * - Payment processing occurs after ride completion
 * 
 * Business Logic:
 * - Driver receives 100% of fare in cash from rider
 * - Platform fee is deducted from driver's wallet balance
 * - No money transfer between rider and driver wallets
 * - Commission collection ensures platform sustainability
 * 
 * Risk Considerations:
 * - Requires sufficient driver wallet balance for commission
 * - Driver must have received cash payment from rider
 * - No built-in payment failure recovery (cash already exchanged)
 * 
 * Use Cases:
 * - Riders who prefer cash transactions
 * - Areas with limited digital payment adoption
 * - Backup payment method when wallet payments fail
 * - Markets with cash-dominant economies
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see PaymentStrategy
 * @see com.redizego.redi_ze_go.strategies.impl.WalletPaymentStrategy
 * @see com.redizego.redi_ze_go.strategies.PaymentStrategyManager
 * @see com.redizego.redi_ze_go.services.WalletService
 * 
 * @implNote Uses @Transactional to ensure atomic commission collection
 * @implNote Assumes cash payment has already occurred between rider and driver
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CashPaymentStrategy implements PaymentStrategy {

    /** Service for managing wallet operations and transactions */
    private final WalletService walletService;
    
    /** Repository for persisting payment records */
    private final PaymentRepository paymentRepository;
    
    /**
     * Processes cash payment by collecting platform commission from driver.
     * 
     * This method handles the post-ride payment processing for cash transactions.
     * Since the rider has already paid the driver in cash, this method focuses on
     * collecting the platform's commission from the driver's wallet and updating
     * the payment status.
     * 
     * Processing Steps:
     * 1. Extract driver information from payment record
     * 2. Calculate platform commission (percentage of total fare)
     * 3. Deduct commission from driver's wallet
     * 4. Mark payment as completed
     * 5. Persist updated payment status
     * 
     * Financial Flow:
     * - Rider → Driver: Full fare amount (in cash, already completed)
     * - Driver Wallet → Platform: Commission amount (via wallet deduction)
     * 
     * @param payment The payment record containing ride and amount information
     * @throws IllegalArgumentException if payment is null or invalid
     * @throws RuntimeException if driver wallet has insufficient balance
     * @throws RuntimeException if wallet service operation fails
     * 
     * @implNote Transaction ensures atomic commission collection and status update
     * @implNote Driver must have sufficient wallet balance for platform commission
     * 
     * @example
     * For a $20 ride with 15% platform fee:
     * - Rider pays $20 cash to driver
     * - Platform deducts $3 from driver's wallet
     * - Driver nets $17 ($20 cash - $3 commission)
     */
    @Override
    @Transactional
    public void processPayment(Payment payment) {
        if (payment == null) {
            log.error("Cannot process cash payment: payment is null");
            throw new IllegalArgumentException("Payment cannot be null");
        }
        
        if (payment.getRide() == null || payment.getRide().getDriver() == null) {
            log.error("Cannot process cash payment: ride or driver information is missing");
            throw new IllegalArgumentException("Payment must have valid ride and driver information");
        }
        
        if (payment.getAmount() <= 0) {
            log.error("Cannot process cash payment: invalid amount {}", payment.getAmount());
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        
        try {
            // Extract driver and payment details
            Driver driver = payment.getRide().getDriver();
            double totalFareAmount = payment.getAmount();
            
            // Calculate platform commission
            double platformCommission = totalFareAmount * PLATFORM_FEE;
            
            log.info("Processing cash payment - Ride ID: {}, Total fare: {:.2f}, " +
                    "Platform commission: {:.2f} ({:.1f}%)", 
                    payment.getRide().getId(), totalFareAmount, platformCommission, PLATFORM_FEE * 100);
            
            // Deduct platform commission from driver's wallet
            // Note: Driver already received full fare in cash from rider
            walletService.deductMoneyFromWallet(
                    driver.getUser(), 
                    platformCommission, 
                    null, 
                    payment.getRide(), 
                    TransactionMethods.RIDE
            );
            
            // Update payment status to completed
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);
            
            log.info("Successfully processed cash payment for ride ID: {} - " +
                    "Driver received {:.2f} cash, Platform commission {:.2f} deducted from wallet", 
                    payment.getRide().getId(), totalFareAmount, platformCommission);
            
        } catch (Exception e) {
            log.error("Failed to process cash payment for ride ID: {} - Amount: {:.2f}", 
                    payment.getRide().getId(), payment.getAmount(), e);
            
            // Re-throw exception to trigger transaction rollback
            throw new RuntimeException("Cash payment processing failed: " + e.getMessage(), e);
        }
    }
}
