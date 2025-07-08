package com.redizego.redi_ze_go.services;

import com.redizego.redi_ze_go.dtos.WalletTransactionDto;
import com.redizego.redi_ze_go.entities.WalletTransactions;

public interface WalletTransactionService {

    void createNewWalletTransaction(WalletTransactions walletTransaction);


}
