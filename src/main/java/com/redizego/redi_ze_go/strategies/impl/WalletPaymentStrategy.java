package com.redizego.redi_ze_go.strategies.impl;

import com.redizego.redi_ze_go.entities.Driver;
import com.redizego.redi_ze_go.entities.Payment;
import com.redizego.redi_ze_go.entities.Rider;
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
 * Payment strategy implementation for digital wallet-based ride payments.
 * 
 * This strategy handles cashless payments where the rider's wallet is debited
 * and the driver's wallet is credited through the platform. It provides instant,
 * secure, and traceable payment processing with automatic commission handling.
 * 
 * Payment Flow:
 * 1. Deduct full fare amount from rider's wallet
 * 2. Calculate driver's share (after platform commission)
 * 3. Credit driver's wallet with net amount
 * 4. Platform retains commission automatically
 * 5. Mark payment as completed
 * 
 * Key Characteristics:
 * - Instant payment processing
 * - Requires sufficient rider wallet balance
 * - Automatic commission calculation and retention
 * - Full transaction traceability
 * - Reversible transactions (if needed)
 * 
 * Financial Flow:
 * - Rider Wallet → Platform: Full fare amount
 * - Platform → Driver Wallet: Fare amount minus commission
 * - Platform → Platform: Commission (retained)
 * 
 * Business Logic:
 * - Platform fee is automatically deducted from fare
 * - Driver receives net amount after commission
 * - All transactions are recorded for audit purposes
 * - Atomic transaction ensures payment consistency
 * 
 * Advantages:
 * - No cash handling required
 * - Instant payment confirmation
 * - Automatic commission processing
 * - Complete transaction history
 * - Supports refunds and adjustments
 * 
 * Risk Management:
 * - Pre-payment validation prevents insufficient funds
 * - Transaction rollback on failure
 * - Audit trail for all money movements
 * - Balance validation before processing
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see PaymentStrategy
 * @see com.redizego.redi_ze_go.strategies.impl.CashPaymentStrategy
 * @see com.redizego.redi_ze_go.strategies.PaymentStrategyManager
 * @see com.redizego.redi_ze_go.services.WalletService
 * 
 * @implNote Uses @Transactional to ensure atomic payment processing
 * @implNote All wallet operations are logged for audit and debugging
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletPaymentStrategy implements PaymentStrategy {

    /** Service for managing wallet operations and transactions */
    private final WalletService walletService;
    
    /** Repository for persisting payment records */
    private final PaymentRepository paymentRepository;

    /**
     * Processes digital wallet payment with automatic commission handling.
     * 
     * This method orchestrates the complete wallet-to-wallet payment flow,
     * including rider deduction, commission calculation, driver credit, and
     * payment status updates. All operations are wrapped in a transaction
     * to ensure consistency and rollback capability.
     * 
     * Processing Steps:
     * 1. Extract rider and driver from payment record
     * 2. Validate payment amount and wallet balances
     * 3. Deduct full fare from rider's wallet
     * 4. Calculate driver's net amount (fare - commission)
     * 5. Credit driver's wallet with net amount
     * 6. Mark payment as completed
     * 7. Persist updated payment status
     * 
     * Financial Calculation:
     * - Platform Commission = Total Fare × PLATFORM_FEE
     * - Driver's Cut = Total Fare × (1 - PLATFORM_FEE)
     * 
     * @param payment The payment record containing ride and amount information
     * @throws IllegalArgumentException if payment is null or invalid
     * @throws RuntimeException if rider has insufficient wallet balance
     * @throws RuntimeException if any wallet operation fails
     * 
     * @implNote Transaction ensures atomic payment processing with rollback on failure
     * @implNote All wallet operations are logged with transaction details
     * 
     * @example
     * For a $20 ride with 15% platform fee:
     * - Rider wallet: -$20.00
     * - Platform commission: $3.00 (retained)
     * - Driver wallet: +$17.00 ($20 - $3 commission)
     */
    @Override
    @Transactional
    public void processPayment(Payment payment) {
        if (payment == null) {
            log.error("Cannot process wallet payment: payment is null");
            throw new IllegalArgumentException("Payment cannot be null");
        }
        
        if (payment.getRide() == null) {
            log.error("Cannot process wallet payment: ride information is missing");
            throw new IllegalArgumentException("Payment must have valid ride information");
        }
        
        if (payment.getAmount() <= 0) {
            log.error("Cannot process wallet payment: invalid amount {}", payment.getAmount());
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        
        try {
            // Extract participants from ride
            Driver driver = payment.getRide().getDriver();
            Rider rider = payment.getRide().getRider();
            
            if (driver == null || rider == null) {
                log.error("Cannot process payment: driver or rider is null");
                throw new IllegalArgumentException("Both driver and rider must be present");
            }
            
            double totalFareAmount = payment.getAmount();
            double platformCommission = totalFareAmount * PLATFORM_FEE;
            double driversCut = totalFareAmount * (1 - PLATFORM_FEE);
            
            log.info("Processing wallet payment - Ride ID: {}, Total fare: {:.2f}, " +
                    "Platform commission: {:.2f} ({:.1f}%), Driver's cut: {:.2f}", 
                    payment.getRide().getId(), totalFareAmount, platformCommission, 
                    PLATFORM_FEE * 100, driversCut);
            
            // Step 1: Deduct full fare amount from rider's wallet
            log.debug("Deducting {:.2f} from rider's wallet for ride ID: {}", 
                    totalFareAmount, payment.getRide().getId());
            
            walletService.deductMoneyFromWallet(
                    rider.getUser(), 
                    totalFareAmount, 
                    null, 
                    payment.getRide(), 
                    TransactionMethods.RIDE
            );
            
            // Step 2: Credit driver's wallet with net amount (after commission)
            log.debug("Crediting {:.2f} to driver's wallet for ride ID: {}", 
                    driversCut, payment.getRide().getId());
            
            walletService.addMoneyToWallet(
                    driver.getUser(), 
                    driversCut, 
                    null, 
                    payment.getRide(), 
                    TransactionMethods.RIDE
            );
            
            // Step 3: Update payment status
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);
            
            log.info("Successfully processed wallet payment for ride ID: {} - " +
                    "Rider charged: {:.2f}, Driver credited: {:.2f}, Platform commission: {:.2f}", 
                    payment.getRide().getId(), totalFareAmount, driversCut, platformCommission);
            
        } catch (Exception e) {
            log.error("Failed to process wallet payment for ride ID: {} - Amount: {:.2f}", 
                    payment.getRide().getId(), payment.getAmount(), e);
            
            // Re-throw exception to trigger transaction rollback
            throw new RuntimeException("Wallet payment processing failed: " + e.getMessage(), e);
        }
    }
}
