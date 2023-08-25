package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class CreditCardController {

    // TODO: wire in CreditCard repository here (~1 line)
    @Autowired CreditCardRepository CreditCardRepo;
    @Autowired UserRepository UserRepo;
    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        // TODO: Create a credit card entity, and then associate that credit card with user with given userId
        //       Return 200 OK with the credit card id if the user exists and credit card is successfully associated with the user
        //       Return other appropriate response code for other exception cases
        //       Do not worry about validating the card number, assume card number could be any arbitrary format and length


        // Retrieve the user based on the provided userId
        Optional<User> currUser = UserRepo.findById(payload.getUserId());

        // Check if the user exists
        if (currUser.isEmpty()) {
            // User not found, return 404 Not Found with the provided userId
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(payload.getUserId());
        }

        // Create a new CreditCard entity and associate it with the current user
        CreditCard newCreditCard = new CreditCard();
        newCreditCard.setUser(currUser.get());
        newCreditCard.setNumber(payload.getCardNumber());

        // Save the new CreditCard entity in the repository
        CreditCardRepo.save(newCreditCard);

        // Return 200 OK with the provided userId, indicating success
        return ResponseEntity.status(HttpStatus.OK).body(payload.getUserId());
    }

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        // TODO: return a list of all credit card associated with the given userId, using CreditCardView class
        //       if the user has no credit card, return empty list, never return null

        // Retrieve the user based on the provided userId
        Optional<User> currUser = UserRepo.findById(userId);

        // Create a list to hold the credit card views
        List<CreditCardView> creditCardViews = new ArrayList<>();

        // Check if the user exists
        if (currUser.isPresent()) {
            // Map the user's credit cards to CreditCardView objects
            creditCardViews = currUser.get().getCreditCard().stream()
                    .map(creditCard -> new CreditCardView(creditCard.getIssuanceBank(), creditCard.getNumber()))
                    .toList();

            // Return 200 OK with the list of credit card views
            return ResponseEntity.ok(creditCardViews);
        }

        // Return 404 Not Found with an empty list of credit card views
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(creditCardViews);
}



    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        // TODO: Given a credit card number, efficiently find whether there is a user associated with the credit card
        //       If so, return the user id in a 200 OK response. If no such user exists, return 400 Bad Request

        Optional<CreditCard> currCreditCard = CreditCardRepo.findByNumber(creditCardNumber);

        // Check if the credit card exists
        if (currCreditCard.isPresent()) {
            // Return 200 OK with the associated user ID
            return ResponseEntity.status(HttpStatus.OK).body(currCreditCard.get().getUser().getId());
        }

        // Return 400 Bad Request if no user is associated with the credit card number
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<Object> postMethodName(@RequestBody UpdateBalancePayload[] payload) {
        //TODO: Given a list of transactions, update credit cards' balance history.
        //      For example: if today is 4/12, a credit card's balanceHistory is [{date: 4/12, balance: 110}, {date: 4/10, balance: 100}],
        //      Given a transaction of {date: 4/10, amount: 10}, the new balanceHistory is
        //      [{date: 4/12, balance: 120}, {date: 4/11, balance: 110}, {date: 4/10, balance: 110}]
        //      Return 200 OK if update is done and successful, 400 Bad Request if the given card number
        //        is not associated with a card.

        // Sort transactions based on transaction time
       payload = Arrays.stream(payload)
                .sorted(Comparator.comparing(UpdateBalancePayload::getTransactionTime))
                .toArray(UpdateBalancePayload[]::new);

        // Iterate through each transaction in the payload
        for (UpdateBalancePayload update : payload) {
            // Set transaction time to the current date at the start of the day in UTC
            LocalDate currDate = LocalDate.now();
            LocalDateTime currDateTime = currDate.atStartOfDay();
            Instant instant = currDateTime.toInstant(ZoneOffset.UTC);
            update.setTransactionTime(instant);
        }

        // Iterate through each transaction in the payload to update credit card balance history
        for (UpdateBalancePayload update : payload) {
            // Retrieve the credit card based on the transaction's credit card number
            Optional<CreditCard> currCreditCard = CreditCardRepo.findByNumber(update.getCreditCardNumber());
            if (currCreditCard.isEmpty()) {
                // Return 400 Bad Request if the credit card is not found
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Check if the transaction's balance history exists
            CreditCard validCreditCard = currCreditCard.get();
            List<BalanceHistory> cardHistory = validCreditCard.getBalanceHistory();
            Optional<BalanceHistory> currBalanceHistory = validCreditCard
                    .getBalanceHistory()
                    .stream()
                    .filter(history -> history.getDate().equals(update.getTransactionTime()))
                    .findFirst();

            // Update balance history based on the transaction
            if (currBalanceHistory.isEmpty()) {
                cardHistory.add(new BalanceHistory(update.getTransactionTime(), update.getTransactionAmount()));
            } else {
                currBalanceHistory.get().setBalance(currBalanceHistory.get().getBalance() + update.getTransactionAmount());
            }
            // Save the updated credit card
            CreditCardRepo.save(validCreditCard);
        }
        // Return 200 OK to indicate successful update
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    }
