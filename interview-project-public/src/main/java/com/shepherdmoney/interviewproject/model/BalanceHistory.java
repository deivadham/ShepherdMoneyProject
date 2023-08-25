package com.shepherdmoney.interviewproject.model;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class BalanceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    private Instant date;

    private double balance;


    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "CreditCardID")
    private CreditCard creditCard;

    //Constructor for credit card balance history
    public BalanceHistory(Instant date, double balance){
        this.date = date;
        this.balance = balance;
    }

}
