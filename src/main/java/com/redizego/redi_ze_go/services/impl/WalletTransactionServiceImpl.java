package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.dtos.WalletTransactionDto;
import com.redizego.redi_ze_go.entities.WalletTransactions;
import com.redizego.redi_ze_go.repositories.WalletTransactionsRepository;
import com.redizego.redi_ze_go.services.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletTransactionServiceImpl implements WalletTransactionService {

    private final WalletTransactionsRepository walletTransactionsRepository;

    @Override
    public void createNewWalletTransaction(WalletTransactions walletTransaction) {
        walletTransactionsRepository.save(walletTransaction);
    }
}
