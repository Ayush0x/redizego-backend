package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.entities.WalletTransactions;
import com.redizego.redi_ze_go.repositories.WalletTransactionsRepository;
import com.redizego.redi_ze_go.services.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service implementation for wallet transaction management.
 * 
 * This service handles the persistence and management of individual
 * wallet transactions, providing audit trail and transaction history
 * functionality for the digital wallet system.
 * 
 * Key Features:
 * - Transaction record creation and persistence
 * - Transaction audit trail maintenance
 * - Integration with wallet balance operations
 * - Support for different transaction types and methods
 * 
 * Transaction Tracking:
 * Every wallet operation (credit/debit) generates a transaction record
 * that includes transaction ID, amount, type, method, timestamp, and
 * associated ride information (if applicable).
 * 
 * This service ensures comprehensive transaction logging for:
 * - Regulatory compliance
 * - User transaction history
 * - Dispute resolution
 * - Financial reconciliation
 * - Fraud detection and prevention
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class WalletTransactionServiceImpl implements WalletTransactionService {

    /** Repository for wallet transaction data access and persistence */
    private final WalletTransactionsRepository walletTransactionsRepository;

    /**
     * Creates and persists a new wallet transaction record.
     * 
     * This method saves individual transaction records to maintain
     * a complete audit trail of all wallet operations. Each transaction
     * includes details about the amount, type, method, associated ride,
     * and timestamp information.
     * 
     * Transaction records are immutable once created and serve as:
     * - Proof of payment/receipt
     * - Audit trail for compliance
     * - History for user reference
     * - Data for financial reporting
     * - Evidence for dispute resolution
     * 
     * @param walletTransaction The complete transaction details to persist
     */
    @Override
    public void createNewWalletTransaction(WalletTransactions walletTransaction) {
        walletTransactionsRepository.save(walletTransaction);
    }
}
