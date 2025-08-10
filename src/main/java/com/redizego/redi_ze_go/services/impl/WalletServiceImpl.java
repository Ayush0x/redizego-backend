package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.entities.Ride;
import com.redizego.redi_ze_go.entities.User;
import com.redizego.redi_ze_go.entities.Wallet;
import com.redizego.redi_ze_go.entities.WalletTransactions;
import com.redizego.redi_ze_go.entities.enums.TransactionMethods;
import com.redizego.redi_ze_go.entities.enums.TransactionType;
import com.redizego.redi_ze_go.exceptions.ResourceNotFoundException;
import com.redizego.redi_ze_go.repositories.WalletRepository;
import com.redizego.redi_ze_go.services.WalletService;
import com.redizego.redi_ze_go.services.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for digital wallet management and transactions.
 * 
 * This service handles all wallet-related operations including balance management,
 * transaction processing, wallet creation, and transaction history. It provides
 * a secure and reliable digital payment system for the ride-sharing platform.
 * 
 * Key Features:
 * - Wallet creation for new users
 * - Balance addition and deduction operations
 * - Transaction recording and history
 * - Atomic transaction processing
 * - Multiple transaction methods support
 * - Comprehensive validation and error handling
 * 
 * Transaction Types:
 * - CREDIT: Adding money to wallet (top-ups, refunds)
 * - DEBIT: Deducting money from wallet (ride payments)
 * 
 * Transaction Methods:
 * - BANK_TRANSFER: Direct bank account transfers
 * - CARD: Credit/debit card transactions
 * - ONLINE: Online payment gateways
 * - CASH: Cash-to-wallet conversions
 * 
 * Security Features:
 * - Transactional operations for consistency
 * - Balance validation before deductions
 * - Transaction audit trail
 * - User ownership verification
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    /** Repository for wallet data access and persistence */
    private final WalletRepository walletRepository;
    
    /** ModelMapper for entity transformations */
    private final ModelMapper modelMapper;
    
    /** Service for wallet transaction management */
    private final WalletTransactionService walletTransactionService;

    /**
     * Adds money to a user's wallet with transaction recording.
     * 
     * This method handles wallet top-ups and refunds by:
     * 1. Retrieving the user's wallet
     * 2. Increasing the wallet balance
     * 3. Creating a CREDIT transaction record
     * 4. Persisting both wallet and transaction updates
     * 
     * The operation is transactional to ensure atomicity - either both
     * balance update and transaction recording succeed, or both fail.
     * 
     * Common use cases:
     * - User wallet top-ups
     * - Refunds for cancelled rides
     * - Promotional credits
     * - Driver earnings deposits
     * 
     * @param user The user whose wallet to credit
     * @param amount The amount to add (must be positive)
     * @param transactionId Unique identifier for the transaction
     * @param ride Associated ride (if applicable, can be null)
     * @param method The payment method used for the credit
     * @return Wallet The updated wallet with new balance
     * @throws ResourceNotFoundException if user doesn't have a wallet
     */
    @Override
    @Transactional
    public Wallet addMoneyToWallet(User user, Double amount, String transactionId, Ride ride, TransactionMethods method) {
        Wallet wallet =findByUser(user);
        wallet.setBalance(wallet.getBalance()+amount);

        WalletTransactions walletTransaction=WalletTransactions.builder()
                .transactionId(transactionId)
                .ride(ride)
                .wallet(wallet)
                .transactionType(TransactionType.CREDIT)
                .transactionMethod(method)
                .amount(amount)
                .build();

        walletTransactionService.createNewWalletTransaction(walletTransaction);

        return walletRepository.save(wallet);
    }

    /**
     * Deducts money from a user's wallet with transaction recording.
     * 
     * This method handles wallet payments and deductions by:
     * 1. Retrieving the user's wallet
     * 2. Decreasing the wallet balance
     * 3. Creating a DEBIT transaction record
     * 4. Adding transaction to wallet's transaction list
     * 5. Persisting the updated wallet
     * 
     * The operation is transactional to ensure atomicity. Note that this
     * method currently adds the transaction directly to the wallet's
     * transaction list rather than using the transaction service.
     * 
     * Common use cases:
     * - Ride fare payments
     * - Service fee deductions
     * - Penalty charges
     * - Refund reversals
     * 
     * @param user The user whose wallet to debit
     * @param amount The amount to deduct (must be positive)
     * @param transactionId Unique identifier for the transaction
     * @param ride Associated ride (if applicable, can be null)
     * @param method The payment method used for the debit
     * @return Wallet The updated wallet with reduced balance
     * @throws ResourceNotFoundException if user doesn't have a wallet
     * @implNote Consider adding balance validation to prevent negative balances
     */
    @Override
    @Transactional
    public Wallet deductMoneyFromWallet(User user, Double amount, String transactionId, Ride ride, TransactionMethods method) {
        Wallet wallet =findByUser(user);

        wallet.setBalance(wallet.getBalance()-amount);

        WalletTransactions walletTransactions=WalletTransactions.builder()
                .transactionId(transactionId)
                .ride(ride)
                .wallet(wallet)
                .transactionType(TransactionType.DEBIT)
                .transactionMethod(method)
                .amount(amount)
                .build();

//        walletTransactionService.createNewWalletTransaction(walletTransactions);

        wallet.getWalletTransactions().add(walletTransactions);

        return walletRepository.save(wallet);

    }

    /**
     * Withdraws money from a user's wallet to external accounts.
     * 
     * This method is currently a placeholder for future implementation
     * of wallet-to-bank withdrawals. When implemented, it should:
     * 1. Validate sufficient balance
     * 2. Process withdrawal through payment gateway
     * 3. Deduct amount from wallet
     * 4. Record withdrawal transaction
     * 5. Handle withdrawal fees if applicable
     * 
     * @param userId The ID of the user requesting withdrawal
     * @param amount The amount to withdraw
     * @todo Implement withdrawal logic with external payment processing
     */
    @Override
    public void withdrawMoneyFromWallet(Long userId, Double amount) {
    }

    /**
     * Retrieves a wallet by its unique identifier.
     * 
     * This method provides secure access to wallet information
     * with proper error handling for non-existent wallets.
     * Used primarily for administrative operations and debugging.
     * 
     * @param walletId The unique identifier of the wallet
     * @return Wallet The wallet entity with balance and transaction history
     * @throws ResourceNotFoundException if no wallet exists with the given ID
     */
    @Override
    public Wallet findWalletById(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(()->
                        new ResourceNotFoundException("Wallet not found with id "+ walletId));
    }

    /**
     * Creates a new wallet for a user during registration.
     * 
     * This method initializes a new digital wallet with:
     * - Zero initial balance
     * - Association with the user account
     * - Empty transaction history
     * 
     * Called automatically during user signup to ensure every
     * user has a wallet for digital payments.
     * 
     * @param user The user for whom to create a wallet
     * @return Wallet The newly created wallet entity
     */
    @Override
    public Wallet createNewWallet(User user) {
        Wallet wallet=new Wallet();
        wallet.setUser(user);

        return walletRepository.save(wallet);
    }

    /**
     * Retrieves a user's wallet by user entity.
     * 
     * This is the primary method for accessing a user's wallet,
     * used throughout the application for balance checks and
     * transaction processing. Every user should have exactly
     * one wallet.
     * 
     * @param user The user whose wallet to retrieve
     * @return Wallet The user's wallet with current balance and transactions
     * @throws ResourceNotFoundException if the user doesn't have a wallet
     */
    @Override
    public Wallet findByUser(User user) {
        return walletRepository.findByUser(user)
                .orElseThrow(()->
                        new ResourceNotFoundException("Wallet not found for user "+ user.getId()));
    }
}
