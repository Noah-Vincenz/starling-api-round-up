package com.starling.savingsgoalcreator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.starling.savingsgoalcreator.clientmodels.v2.Accounts;
import com.starling.savingsgoalcreator.clientmodels.v2.SavingsGoalsV2;
import com.starling.savingsgoalcreator.model.SavingsGoalCreationApiResponse;

//import static com.starling.savingsgoalcreator.config.HeaderFilter.put;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsGoalCreatorService {

    private final StarlingApiRequestService starlingApiRequestService;

    public Flux<SavingsGoalCreationApiResponse> createSavingsGoal(String savingsGoalName, String minDate, String maxDate) {
        // 1. get all account id
        // 2. for each account id, call get transactions, accumulate transactions amount
        // 3. create new savings goal
        // 4. add money into savings goal
        return starlingApiRequestService.getAllAccounts().flatMapMany(accounts -> {
            List<Mono<SavingsGoalCreationApiResponse>> listOfResponses = new ArrayList<>();
            accounts.getAccounts().forEach(account -> {
                Mono<SavingsGoalCreationApiResponse> responseMono = createGoal(account.getAccountUid(), savingsGoalName, minDate, maxDate);
                listOfResponses.add(responseMono);
            });
            return Flux.concat(listOfResponses);
        });
    }

    public Mono<SavingsGoalCreationApiResponse> createSavingsGoal(UUID accountId, String savingsGoalName, String minDate, String maxDate) {
        return createGoal(accountId, savingsGoalName, minDate, maxDate);
    }

    private Mono<SavingsGoalCreationApiResponse> createGoal(UUID accountId, String savingsGoalName, String minDate, String maxDate) {
        // check if goal with specific names exists already
        return starlingApiRequestService.getAllSavingsGoals(accountId).flatMap(savingsGoals -> {
            boolean savingsGoalNameExists = savingsGoals.getSavingsGoalList()
                                                                            .stream()
                                                                            .anyMatch(it -> it.getName().equals(savingsGoalName));
            String requestOutcomeMessage;
            if (savingsGoalNameExists) {
                requestOutcomeMessage = String.format("Savings goal with name [%s] already exists for account [%s] so no additional savings goal was created", savingsGoalName, accountId);
                log.info(requestOutcomeMessage);
                return Mono.just(new SavingsGoalCreationApiResponse(accountId, false, requestOutcomeMessage, savingsGoals.getSavingsGoalList().size(), savingsGoals));
            } else {
                return starlingApiRequestService.getAccountTransactions(accountId, minDate, maxDate).flatMap(transactions -> {
                    long accumulatedRoundUpsForThisAccount = transactions.getFeedItems()
                                                                                                  .stream()
                                                                                                  .filter(transaction -> transaction.getDirection().equals("OUT"))
                                                                                                  .map(outgoingTransaction -> {
                                                                                                      log.debug("Found an outgoing transaction for account [{}] with amount [{}]", accountId, outgoingTransaction.getAmount().getMinorUnits());
                                                                                                      long originalAmount = outgoingTransaction.getAmount().getMinorUnits();
                                                                                                      long roundedUpAmount = roundUpToNearest100(originalAmount);
                                                                                                      return roundedUpAmount - originalAmount;
                                                                                                  }).mapToLong(Long::longValue).sum();
                    log.debug("Calculated the savings goal amount to be [{}] pence", accumulatedRoundUpsForThisAccount);
                    // create new savings goal
                    return starlingApiRequestService.createSavingsGoal(accountId, savingsGoalName).flatMap(savingsGoalResponse -> {
                        // 4. add money into savings goal
                        return starlingApiRequestService.addMoneyIntoSavingsGoal(accountId,
                                                                          savingsGoalResponse.getSavingsGoalUid(),
                                                                          accumulatedRoundUpsForThisAccount).flatMap(resp -> {
                            return starlingApiRequestService.getAllSavingsGoals(accountId).flatMap(savingsGoals2 -> {
                                String requestOutcomeMessage2 = String.format("Successfully added new savings goal with name [%s] for account with id [%s] and transferred [%s] pence into it", savingsGoalName, accountId, accumulatedRoundUpsForThisAccount);
                                log.info(requestOutcomeMessage2);
                                return Mono.just(new SavingsGoalCreationApiResponse(accountId, true, requestOutcomeMessage2, savingsGoals2.getSavingsGoalList().size(), savingsGoals2));
                            });
                        });
                    });
                });
            }
        });
    }

    protected long roundUpToNearest100(long input) {
        return ((input + 99) / 100 ) * 100;
    }
}
