package com.redizego.redi_ze_go.services;

import com.redizego.redi_ze_go.entities.Ride;
import com.redizego.redi_ze_go.entities.User;
import com.redizego.redi_ze_go.entities.Wallet;
import com.redizego.redi_ze_go.entities.enums.TransactionMethods;

public interface WalletService {

    Wallet addMoneyToWallet(User user, Double amount, String transactionId, Ride ride, TransactionMethods method);

    Wallet deductMoneyFromWallet(User user,Double amount, String transactionId, Ride ride, TransactionMethods method);

    void withdrawMoneyFromWallet(Long userId,Double amount);

    Wallet findWalletById(Long walletId);

    Wallet createNewWallet(User user);

    Wallet findByUser(User user);
}
