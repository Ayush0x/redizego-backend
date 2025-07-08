package com.redizego.redi_ze_go.dtos;

import com.redizego.redi_ze_go.entities.enums.TransactionMethods;
import com.redizego.redi_ze_go.entities.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalletTransactionDto {

    private Long id;

    private Double amount;

    private TransactionType transactionType;

    private TransactionMethods method;

    private RideDto ride;

    private String transactionId;

    private WalletDto wallet;

    private LocalDateTime timestamp;
}
