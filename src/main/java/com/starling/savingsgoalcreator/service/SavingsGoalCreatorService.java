package com.starling.savingsgoalcreator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.starling.savingsgoalcreator.clientmodels.v2.CreateOrUpdateSavingsGoalResponseV2;
import com.starling.savingsgoalcreator.clientmodels.v2.FeedItems;
import com.starling.savingsgoalcreator.model.SavingsGoalCreationApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsGoalCreatorService {

    private static final String DIRECTION_OUTGOING = "OUT";

    private final StarlingApiRequestService starlingApiRequestService;

    /**
     * Create a savings goal for all the user's accounts.
     * This function consists of the following steps:
     * <li>get all account ids</li>
     * <li>for each account id, try and create a new savings goal</li>
     * <li>for each account id, return the outcome</li>
     * @return The outcome of creating a new savings goal for every account the user owns.
     */
    public Flux<SavingsGoalCreationApiResponse> createSavingsGoal(String savingsGoalName, String minDate, String maxDate) {
        return starlingApiRequestService.getAllAccounts().flatMapMany(accounts -> {
            List<Mono<SavingsGoalCreationApiResponse>> listOfResponses = new ArrayList<>();
            accounts.getAccounts().forEach(account -> {
                // for each account create a savings goal
                Mono<SavingsGoalCreationApiResponse> responseMono = createSavingsGoal(account.getAccountUid(), savingsGoalName, minDate, maxDate);
                listOfResponses.add(responseMono);
            });
            // combine multiple lists of Mono responses into a single list ("Flux") of those responses
            return Flux.concat(listOfResponses);
        });
    }

    /**
     * Create a savings goal for a given user account. This only creates a new savings goal if the user does not yet have a savings goal
     * for the given account with the given name.
     * <li>1. for the given account id, get all savings goals to check whether the given savings goal name already exists - proceed to step 6 if so</li>
     * <li>2. for the given account id, get all transactions and accumulate all rounded-up amounts</li>
     * <li>3. for the given account id, create a new savings goal if the given savings goal name does not yet exist</li>
     * <li>4. for the given account id, transfer the accumulated round-up amount into the newly created savings goal</li>
     * <li>5. for the given account id, get all savings goals again which will be returned in the response</li>
     * <li>6. for the given account id, return the outcome of the process above</li>
     * @return The outcome of creating a new savings goal for a given account the user owns
     */
    public Mono<SavingsGoalCreationApiResponse> createSavingsGoal(UUID accountId, String savingsGoalName, String minDate, String maxDate) {
        // 1. get existing savings goals
        return starlingApiRequestService.getAllSavingsGoals(accountId).flatMap(currentSavingsGoals -> {
            boolean savingsGoalNameExists = currentSavingsGoals.getSavingsGoalList()
                                                        .stream()
                                                        .anyMatch(it -> it.getName().equals(savingsGoalName));
            // check if goal with specific name exists already
            if (savingsGoalNameExists) {
                String requestOutcomeMessage = String.format("Savings goal with name [%s] already exists for account [%s] so no additional savings goal was created", savingsGoalName, accountId);
                log.info(requestOutcomeMessage);
                return Mono.just(new SavingsGoalCreationApiResponse(accountId, false, requestOutcomeMessage, currentSavingsGoals.getSavingsGoalList().size(), currentSavingsGoals));
            } else {
                log.debug("Creating a new savings goal with name [{}] for account [{}]", savingsGoalName, accountId);
                // 2. get all account transactions
                return starlingApiRequestService.getAccountTransactions(accountId, minDate, maxDate)
                                                .flatMap(transactions -> {
                                                    long accumulatedRoundUpsForThisAccount = getAccumulatedRoundUpsForListOfTransactions(transactions);
                                                    // 3. create new savings goal
                                                    var createSavingsGoalResponse = starlingApiRequestService.createSavingsGoal(accountId, savingsGoalName);
                                                    return Mono.zip(Mono.just(accumulatedRoundUpsForThisAccount), createSavingsGoalResponse);
                                                })
                                                .flatMap(tuple -> {
                                                    long accumulatedRoundUpsForThisAccount = tuple.getT1();
                                                    CreateOrUpdateSavingsGoalResponseV2 savingsGoalCreationResponse = tuple.getT2();
                                                    UUID savingsGoalId = savingsGoalCreationResponse.getSavingsGoalUid();
                                                    // 4. transfer accumulated round-up sum into savings goal
                                                    var transferResponse = starlingApiRequestService.addMoneyIntoSavingsGoal(accountId, savingsGoalId, accumulatedRoundUpsForThisAccount);
                                                    return Mono.zip(Mono.just(accumulatedRoundUpsForThisAccount), transferResponse);
                                                })
                                                .flatMap(tuple -> {
                                                    long accumulatedRoundUpsForThisAccount = tuple.getT1();
                                                    // 5. get all savings goals again
                                                    var getSavingsGoalsResponse = starlingApiRequestService.getAllSavingsGoals(accountId);
                                                    return Mono.zip(Mono.just(accumulatedRoundUpsForThisAccount), getSavingsGoalsResponse);
                                                })
                                                .map(tuple -> {
                                                    long accumulatedRoundUpsForThisAccount = tuple.getT1();
                                                    var savingsGoalRetrievalResponse = tuple.getT2();
                                                    String requestOutcomeMessage = String.format("Successfully created a new savings goal with name [%s] for account [%s] and moved [%s] pence into it", savingsGoalName, accountId, accumulatedRoundUpsForThisAccount);
                                                    log.info(requestOutcomeMessage);
                                                    // 6. construct the outcome of creating a new savings goal
                                                    return new SavingsGoalCreationApiResponse(accountId, true, requestOutcomeMessage, savingsGoalRetrievalResponse.getSavingsGoalList().size(), savingsGoalRetrievalResponse);
                                                });
            }
        });
    }

    /**
     * Get the accumulated sum of rounded up amounts from a list of transactions.
     * For example if we have a list of 3 numbers, 901, 16, 555, then the number returned by this method will be 228 (99+84+45).
     * @param transactions The list of transactions, each of which we want to round up to the nearest 100.
     * @return The accumulated sum.
     */
    protected long getAccumulatedRoundUpsForListOfTransactions(FeedItems transactions) {
        var result = transactions.getFeedItems()
                                 .stream()
                                 .filter(transaction -> transaction.getDirection().equals(DIRECTION_OUTGOING))
                                 .map(outgoingTransaction -> {
                                     log.debug("Found an outgoing transaction with amount [{}]", outgoingTransaction.getAmount().getMinorUnits());
                                     long originalAmount = outgoingTransaction.getAmount().getMinorUnits();
                                     long roundedUpAmount = roundUpToNearest100(originalAmount);
                                     return roundedUpAmount - originalAmount;
                                 })
                                 .mapToLong(Long::longValue)
                                 .sum();
        log.debug("Calculated the savings goal amount to be [{}] pence", result);
        return result;
    }

    /**
     * Round up a given (long) number to the nearest 100.
     * <li>901->1000</li>
     * <li>99->100</li>
     * <li>16->100</li>
     * <li>555->600</li>
     * @return the resulting rounded up number
     */
    private long roundUpToNearest100(long input) {
        return ((input + 99) / 100 ) * 100;
    }
}
