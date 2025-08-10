package com.redizego.redi_ze_go.strategies;

import com.redizego.redi_ze_go.entities.enums.PaymentMethod;
import com.redizego.redi_ze_go.strategies.impl.CashPaymentStrategy;
import com.redizego.redi_ze_go.strategies.impl.WalletPaymentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Strategy manager for payment processing decisions in the ride-hailing system.
 * 
 * This class serves as a factory for selecting appropriate payment strategies
 * based on the payment method chosen by the user. It implements the Strategy Pattern
 * to handle different payment types with their specific processing logic.
 * 
 * Supported Payment Methods:
 * - WALLET: Digital wallet payments with instant processing
 * - CASH: Cash payments handled at ride completion
 * 
 * Key Responsibilities:
 * - Payment strategy selection based on user preference
 * - Encapsulation of payment-specific business logic
 * - Extensible design for adding new payment methods
 * 
 * Design Benefits:
 * - Single Responsibility: Each strategy handles one payment type
 * - Open/Closed Principle: Easy to add new payment methods without modification
 * - Strategy Pattern: Runtime selection of payment processing logic
 * 
 * Future Extensions:
 * - Credit card payments
 * - UPI/Digital payments
 * - Corporate account payments
 * - Split payments
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see PaymentStrategy
 * @see PaymentMethod
 * @see CashPaymentStrategy
 * @see WalletPaymentStrategy
 * 
 * @implNote Uses Java 17+ switch expressions for concise strategy selection
 * @implNote Thread-safe as all strategies are stateless
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStrategyManager {

    /** Strategy for processing digital wallet payments */
    private final CashPaymentStrategy cashPaymentStrategy;
    
    /** Strategy for processing cash payments */
    private final WalletPaymentStrategy walletPaymentStrategy;

    /**
     * Returns the appropriate payment strategy based on the selected payment method.
     * 
     * This method acts as a factory method that selects and returns the correct
     * payment strategy implementation based on the payment method enum value.
     * Each strategy encapsulates the specific logic required for processing
     * that type of payment.
     * 
     * Payment Method Mapping:
     * - PaymentMethod.WALLET → WalletPaymentStrategy
     *   → Instant deduction from user's digital wallet
     *   → Balance validation and transaction recording
     *   → Immediate payment confirmation
     * 
     * - PaymentMethod.CASH → CashPaymentStrategy
     *   → Deferred payment collection at ride end
     *   → No upfront balance validation required
     *   → Payment confirmation upon ride completion
     * 
     * @param paymentMethod The payment method selected by the user
     * @return PaymentStrategy implementation for the specified payment method
     * @throws IllegalArgumentException if payment method is null or unsupported
     * 
     * @implNote Uses switch expression for clean, exhaustive method mapping
     * @implNote Compiler ensures all enum values are handled
     * 
     * @example
     * PaymentStrategy strategy = manager.getPaymentStrategy(PaymentMethod.WALLET);
     * strategy.processPayment(ride, amount);
     */
    public PaymentStrategy getPaymentStrategy(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            log.error("Payment method cannot be null");
            throw new IllegalArgumentException("Payment method is required");
        }
        
        PaymentStrategy strategy = switch (paymentMethod) {
            case WALLET -> {
                log.debug("Selected wallet payment strategy for payment method: {}", paymentMethod);
                yield walletPaymentStrategy;
            }
            case CASH -> {
                log.debug("Selected cash payment strategy for payment method: {}", paymentMethod);
                yield cashPaymentStrategy;
            }
        };
        
        log.debug("Payment strategy selected: {} for method: {}", 
                strategy.getClass().getSimpleName(), paymentMethod);
        
        return strategy;
    }
    
    /**
     * Checks if a payment method is supported by the system.
     * 
     * This utility method can be used by clients to validate payment methods
     * before attempting to process payments.
     * 
     * @param paymentMethod The payment method to check
     * @return true if the payment method is supported, false otherwise
     * 
     * @implNote Currently supports all PaymentMethod enum values
     */
    public boolean isPaymentMethodSupported(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return false;
        }
        
        try {
            getPaymentStrategy(paymentMethod);
            return true;
        } catch (IllegalArgumentException e) {
            log.warn("Unsupported payment method: {}", paymentMethod);
            return false;
        }
    }
}
