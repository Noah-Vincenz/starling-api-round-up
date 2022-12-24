package com.starling.savingsgoalcreator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.starling.savingsgoalcreator.clientmodels.v2.Accounts;
import com.starling.savingsgoalcreator.clientmodels.v2.CreateOrUpdateSavingsGoalResponseV2;
import com.starling.savingsgoalcreator.clientmodels.v2.SavingsGoalsV2;
import com.starling.savingsgoalcreator.model.SavingsGoalCreationApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsGoalCreatorService {

    private final StarlingApiRequestService starlingApiRequestService;

    public ResponseEntity<List<SavingsGoalCreationApiResponse>> createSavingsGoal(String savingsGoalName, String minDate, String maxDate) {
        // 1. get all account id
        // 2. for each account id, call get transactions, accumulate transactions amount
        // 3. create new savings goal
        // 4. add money into savings goal
        List<SavingsGoalCreationApiResponse> listOfResponses = new ArrayList<>();
        ResponseEntity<Accounts> accountsResponse = starlingApiRequestService.getAllAccounts();
        accountsResponse.getBody().getAccounts().forEach(account -> {
            listOfResponses.add(createGoal(account.getAccountUid(), savingsGoalName, minDate, maxDate));
        });
        return ResponseEntity.of(Optional.of(listOfResponses));
    }

    public ResponseEntity<List<SavingsGoalCreationApiResponse>> createSavingsGoal(UUID accountId, String savingsGoalName, String minDate, String maxDate) {
        return ResponseEntity.of(Optional.of(List.of(createGoal(accountId, savingsGoalName, minDate, maxDate))));
    }

    private SavingsGoalCreationApiResponse createGoal(UUID accountId, String savingsGoalName, String minDate, String maxDate) {
        // check if goal with specific names exists already
        String requestOutcomeMessage;
        ResponseEntity<SavingsGoalsV2> savingsGoalsResponse = starlingApiRequestService.getAllSavingsGoals(accountId);
        boolean savingsGoalNameExists = savingsGoalsResponse.getBody()
                                                            .getSavingsGoalList()
                                                            .stream()
                                                            .anyMatch(it -> it.getName().equals(savingsGoalName));
        if (savingsGoalNameExists) {
            requestOutcomeMessage = String.format("Savings goal with name [%s] already exists for account [%s] so no additional savings goal was created", savingsGoalName, accountId);
        } else {
            long accumulatedRoundUpsForThisAccount = starlingApiRequestService.getAccountTransactions(accountId, minDate, maxDate)
                                                                              .getBody()
                                                                              .getFeedItems()
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
            ResponseEntity<CreateOrUpdateSavingsGoalResponseV2> savingsGoalResponse = starlingApiRequestService.createSavingsGoal(accountId, savingsGoalName);
            // 4. add money into savings goal
            starlingApiRequestService.addMoneyIntoSavingsGoal(accountId,
                                                              savingsGoalResponse.getBody().getSavingsGoalUid(),
                                                              accumulatedRoundUpsForThisAccount);

            savingsGoalsResponse = starlingApiRequestService.getAllSavingsGoals(accountId);
            requestOutcomeMessage = String.format("Successfully added new savings goal with name [%s] for account with id [%s] and transferred [%s] pence into it", savingsGoalName, accountId, accumulatedRoundUpsForThisAccount);
        }
        log.info(requestOutcomeMessage);
        return new SavingsGoalCreationApiResponse(accountId, requestOutcomeMessage, savingsGoalsResponse.getBody());
    }

    protected long roundUpToNearest100(long input) {
        return ((input + 99) / 100 ) * 100;
    }
}
