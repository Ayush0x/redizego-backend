package com.redizego.redi_ze_go.entities;

import com.redizego.redi_ze_go.entities.enums.TransactionMethods;
import com.redizego.redi_ze_go.entities.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name="idx_wallet_transaction_wallet",columnList = "wallet_id"),
        @Index(name="idx_wallet_transaction_ride",columnList = "ride_id")
})
public class WalletTransactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionMethods transactionMethod;

    @ManyToOne
    private Ride ride;

    private String transactionId;

    @CreationTimestamp
    private LocalDateTime timestamp;

    @ManyToOne
    private Wallet wallet;


}
