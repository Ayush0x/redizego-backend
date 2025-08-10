package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.entities.Payment;
import com.redizego.redi_ze_go.entities.Ride;
import com.redizego.redi_ze_go.entities.enums.PaymentMethod;
import com.redizego.redi_ze_go.entities.enums.PaymentStatus;
import com.redizego.redi_ze_go.exceptions.ResourceNotFoundException;
import com.redizego.redi_ze_go.repositories.PaymentRepository;
import com.redizego.redi_ze_go.services.PaymentService;
import com.redizego.redi_ze_go.strategies.PaymentStrategyManager;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

/**
 * Service implementation for payment processing and management.
 * 
 * This service handles all payment-related operations including payment creation,
 * processing, and status management. It uses the Strategy pattern to support
 * multiple payment methods (cash, card, wallet) with different processing logic
 * for each payment type.
 * 
 * Key Features:
 * - Payment creation for new rides
 * - Multi-method payment processing (cash, card, wallet)
 * - Payment status management and tracking
 * - Integration with external payment gateways
 * - Strategy-based payment method handling
 * 
 * Payment Lifecycle:
 * 1. Payment created with PENDING status when ride starts
 * 2. Payment processed when ride ends
 * 3. Status updated based on processing result
 * 
 * Supported Payment Methods:
 * - Cash: Direct cash payment to driver
 * - Card: Credit/debit card processing via payment gateway
 * - Wallet: Digital wallet deduction from user balance
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    /** Repository for payment data access and persistence */
    private final PaymentRepository paymentRepository;
    
    /** Strategy manager for handling different payment methods */
    private final PaymentStrategyManager paymentStrategyManager;
    
    /** ModelMapper for entity-DTO conversions */
    private final ModelMapper modelMapper;

    /**
     * Processes payment for a completed ride using the appropriate payment strategy.
     * 
     * This method handles the actual payment processing by:
     * 1. Retrieving the payment record associated with the ride
     * 2. Determining the payment method (cash, card, wallet)
     * 3. Delegating to the appropriate payment strategy for processing
     * 4. Updating payment status based on processing result
     * 
     * The payment processing behavior varies by method:
     * - Cash: Marks as completed (no external processing needed)
     * - Card: Processes through payment gateway with validation
     * - Wallet: Deducts amount from user's wallet balance
     * 
     * @param ride The completed ride for which to process payment
     * @throws ResourceNotFoundException if no payment record exists for the ride
     * @throws RuntimeException if payment processing fails
     */
    @Override
    public void processPayment(Ride ride) {
        Payment payment=paymentRepository.findByRide(ride).orElseThrow(
                () -> new ResourceNotFoundException("Payment not found for this ride")
        );
        paymentStrategyManager.getPaymentStrategy(payment.getPaymentMethod()).processPayment(payment);
    }

    /**
     * Creates a new payment record for a ride when it starts.
     * 
     * This method initializes a payment record with:
     * - Associated ride information
     * - Payment method from the ride request
     * - Calculated fare amount
     * - Initial PENDING status
     * 
     * The payment record serves as a transaction log and enables
     * tracking of payment processing throughout the ride lifecycle.
     * 
     * @param ride The ride for which to create a payment record
     * @return Payment The created payment entity with generated ID
     */
    @Override
    public Payment createNewPayment(Ride ride) {
        Payment payment=Payment.builder()
                .ride(ride)
                .paymentMethod(modelMapper.map(ride.getPaymentMethod(), PaymentMethod.class))
                .amount(ride.getFare())
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        return paymentRepository.save(payment);
    }

    /**
     * Updates the status of an existing payment record.
     * 
     * This method allows updating payment status throughout the
     * payment processing lifecycle. Common status transitions:
     * - PENDING → PROCESSING (when payment processing begins)
     * - PROCESSING → COMPLETED (when payment succeeds)
     * - PROCESSING → FAILED (when payment fails)
     * - PENDING → CANCELLED (when ride is cancelled)
     * 
     * @param payment The payment record to update
     * @param paymentStatus The new payment status to set
     */
    @Override
    public void updatePaymentStatus(Payment payment,PaymentStatus paymentStatus) {
        payment.setPaymentStatus(paymentStatus);
        paymentRepository.save(payment);
    }
}
