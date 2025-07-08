package com.redizego.redi_ze_go.services.impl;

import com.redizego.redi_ze_go.dtos.RideDto;
import com.redizego.redi_ze_go.dtos.WalletDto;
import com.redizego.redi_ze_go.dtos.WalletTransactionDto;
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

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final ModelMapper modelMapper;
    private final WalletTransactionService walletTransactionService;

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

    @Override
    public void withdrawMoneyFromWallet(Long userId, Double amount) {
    }

    @Override
    public Wallet findWalletById(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(()->
                        new ResourceNotFoundException("Wallet not found with id "+ walletId));
    }

    @Override
    public Wallet createNewWallet(User user) {
        Wallet wallet=new Wallet();
        wallet.setUser(user);

        return walletRepository.save(wallet);
    }

    @Override
    public Wallet findByUser(User user) {
        return walletRepository.findByUser(user)
                .orElseThrow(()->
                        new ResourceNotFoundException("Wallet not found for user "+ user.getId()));
    }
}
