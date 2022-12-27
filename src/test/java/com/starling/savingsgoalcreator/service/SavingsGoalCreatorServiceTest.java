package com.starling.savingsgoalcreator.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.starling.savingsgoalcreator.clientmodels.v2.AccountV2;
import com.starling.savingsgoalcreator.clientmodels.v2.Accounts;
import com.starling.savingsgoalcreator.clientmodels.v2.CreateOrUpdateSavingsGoalResponseV2;
import com.starling.savingsgoalcreator.clientmodels.v2.CurrencyAndAmount;
import com.starling.savingsgoalcreator.clientmodels.v2.FeedItem;
import com.starling.savingsgoalcreator.clientmodels.v2.FeedItems;
import com.starling.savingsgoalcreator.clientmodels.v2.SavingsGoalTransferResponseV2;
import com.starling.savingsgoalcreator.clientmodels.v2.SavingsGoalV2;
import com.starling.savingsgoalcreator.clientmodels.v2.SavingsGoalsV2;
import com.starling.savingsgoalcreator.model.SavingsGoalCreationApiRequestBody;
import com.starling.savingsgoalcreator.model.SavingsGoalCreationApiResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
class SavingsGoalCreatorServiceTest {

    @Mock
    private StarlingApiRequestService starlingApiRequestService;

    @InjectMocks
    private SavingsGoalCreatorService service;

    private final String TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN);

    @Test
    void test_getAccumulatedRoundUpsForListOfTransactions() {
        FeedItem transaction1 = new FeedItem();
        CurrencyAndAmount amount1 = new CurrencyAndAmount("GBP", Long.parseLong("123"));
        transaction1.setAmount(amount1);
        transaction1.setDirection("OUT");
        FeedItem transaction2 = new FeedItem();
        CurrencyAndAmount amount2 = new CurrencyAndAmount("GBP", Long.parseLong("1204"));
        transaction2.setAmount(amount2);
        transaction2.setDirection("IN");
        FeedItem transaction3 = new FeedItem();
        CurrencyAndAmount amount3 = new CurrencyAndAmount("GBP", Long.parseLong("1234"));
        transaction3.setAmount(amount3);
        transaction3.setDirection("OUT");
        FeedItems transactions = new FeedItems();
        transactions.setFeedItems(List.of(transaction1, transaction2, transaction3));

        long response = service.getAccumulatedRoundUpsForListOfTransactions(transactions);

        assertEquals(Long.parseLong("143"), response);
    }

    @Test
    void test_createSavingsGoal() {
        UUID accountId = UUID.randomUUID();
        String minDate = dateTimeFormatter.format(LocalDateTime.now().minusDays(100));
        String maxDate = dateTimeFormatter.format(LocalDateTime.now());
        SavingsGoalCreationApiRequestBody requestBody = new SavingsGoalCreationApiRequestBody();
        requestBody.setCurrency("GBP");
        requestBody.setSavingsGoalName("Second Car");

        FeedItem transaction1 = new FeedItem();
        CurrencyAndAmount amount1 = new CurrencyAndAmount("GBP", Long.parseLong("123"));
        transaction1.setAmount(amount1);
        transaction1.setDirection("OUT");
        FeedItem transaction2 = new FeedItem();
        CurrencyAndAmount amount2 = new CurrencyAndAmount("GBP", Long.parseLong("1204"));
        transaction2.setAmount(amount2);
        transaction2.setDirection("IN");
        FeedItem transaction3 = new FeedItem();
        CurrencyAndAmount amount3 = new CurrencyAndAmount("GBP", Long.parseLong("1234"));
        transaction3.setAmount(amount3);
        transaction3.setDirection("OUT");
        FeedItems transactions = new FeedItems();
        transactions.setFeedItems(List.of(transaction1, transaction2, transaction3));

        UUID savingsGoalUuid1 = UUID.randomUUID();
        UUID savingsGoalUuid2 = UUID.randomUUID();
        SavingsGoalV2 savingsGoal1 = new SavingsGoalV2();
        savingsGoal1.setSavingsGoalUid(savingsGoalUuid1);
        savingsGoal1.setName("Car");
        SavingsGoalV2 savingsGoal2 = new SavingsGoalV2();
        savingsGoal2.setSavingsGoalUid(savingsGoalUuid2);
        savingsGoal2.setName("House");
        SavingsGoalsV2 savingsGoalsResponse1 = new SavingsGoalsV2();
        List<SavingsGoalV2> savingsGoalV2List = List.of(savingsGoal1, savingsGoal2);
        savingsGoalsResponse1.setSavingsGoalList(savingsGoalV2List);

        UUID newSavingsGoalId = UUID.randomUUID();
        CreateOrUpdateSavingsGoalResponseV2 createOrUpdateSavingsGoalResponseV2 = new CreateOrUpdateSavingsGoalResponseV2();
        createOrUpdateSavingsGoalResponseV2.setSuccess(true);
        createOrUpdateSavingsGoalResponseV2.setSavingsGoalUid(newSavingsGoalId);
        createOrUpdateSavingsGoalResponseV2.setErrors(Collections.emptyList());

        SavingsGoalTransferResponseV2 savingsGoalTransferResponseV2 = new SavingsGoalTransferResponseV2();
        UUID transferId = UUID.randomUUID();
        savingsGoalTransferResponseV2.setTransferUid(transferId);
        savingsGoalTransferResponseV2.setSuccess(true);
        savingsGoalTransferResponseV2.setErrors(Collections.emptyList());

        when(starlingApiRequestService.getAllSavingsGoals(accountId)).thenReturn(Mono.just(savingsGoalsResponse1));
        when(starlingApiRequestService.getAccountTransactions(accountId, minDate, maxDate)).thenReturn(Mono.just(transactions));
        when(starlingApiRequestService.createSavingsGoal(accountId, requestBody)).thenReturn(Mono.just(createOrUpdateSavingsGoalResponseV2));
        when(starlingApiRequestService.addMoneyIntoSavingsGoal(accountId, newSavingsGoalId, requestBody.getCurrency(), Long.parseLong("143"))).thenReturn(Mono.just(savingsGoalTransferResponseV2));

        Mono<SavingsGoalCreationApiResponse> response = service.createSavingsGoal(accountId, requestBody, minDate, maxDate);

        StepVerifier.create(response)
                    .expectNextMatches(savingsGoalCreationApiResponse -> {
                        var accountIdMatches = savingsGoalCreationApiResponse.getAccountId().equals(accountId);
                        var savingsGoalCreatedMatches = savingsGoalCreationApiResponse.isSavingsGoalCreated();
                        var messageMatches = savingsGoalCreationApiResponse.getMessage().equals("Successfully created a new savings goal with name [Second Car] for account [" + accountId + "] and moved [143] pence into it");
                        var itemsMatches = savingsGoalCreationApiResponse.getCurrentSavingsGoals().getSavingsGoalList().equals(savingsGoalV2List);
                        var sizeMatches = savingsGoalCreationApiResponse.getSavingsGoalsCount() == 2;
                        return accountIdMatches && savingsGoalCreatedMatches && messageMatches && itemsMatches && sizeMatches;
                    })
                    .verifyComplete();
    }

    @Test
    void test_createSavingsGoal_ExistingName() {
        UUID accountId = UUID.randomUUID();
        String minDate = dateTimeFormatter.format(LocalDateTime.now().minusDays(100));
        String maxDate = dateTimeFormatter.format(LocalDateTime.now());
        SavingsGoalCreationApiRequestBody requestBody = new SavingsGoalCreationApiRequestBody();
        requestBody.setCurrency("GBP");
        requestBody.setSavingsGoalName("Car");

        FeedItem transaction1 = new FeedItem();
        CurrencyAndAmount amount1 = new CurrencyAndAmount("GBP", Long.parseLong("123"));
        transaction1.setAmount(amount1);
        transaction1.setDirection("OUT");
        FeedItem transaction2 = new FeedItem();
        CurrencyAndAmount amount2 = new CurrencyAndAmount("GBP", Long.parseLong("1204"));
        transaction2.setAmount(amount2);
        transaction2.setDirection("IN");
        FeedItem transaction3 = new FeedItem();
        CurrencyAndAmount amount3 = new CurrencyAndAmount("GBP", Long.parseLong("1234"));
        transaction3.setAmount(amount3);
        transaction3.setDirection("OUT");
        FeedItems transactions = new FeedItems();
        transactions.setFeedItems(List.of(transaction1, transaction2, transaction3));

        UUID savingsGoalUuid1 = UUID.randomUUID();
        UUID savingsGoalUuid2 = UUID.randomUUID();
        SavingsGoalV2 savingsGoal1 = new SavingsGoalV2();
        savingsGoal1.setSavingsGoalUid(savingsGoalUuid1);
        savingsGoal1.setName("Car");
        SavingsGoalV2 savingsGoal2 = new SavingsGoalV2();
        savingsGoal2.setSavingsGoalUid(savingsGoalUuid2);
        savingsGoal2.setName("House");
        SavingsGoalsV2 savingsGoalsResponse1 = new SavingsGoalsV2();
        List<SavingsGoalV2> savingsGoalV2List = List.of(savingsGoal1, savingsGoal2);
        savingsGoalsResponse1.setSavingsGoalList(savingsGoalV2List);

        when(starlingApiRequestService.getAllSavingsGoals(accountId)).thenReturn(Mono.just(savingsGoalsResponse1));

        Mono<SavingsGoalCreationApiResponse> response = service.createSavingsGoal(accountId, requestBody, minDate, maxDate);

        StepVerifier.create(response)
                    .expectNextMatches(savingsGoalCreationApiResponse -> {
                        var accountIdMatches = savingsGoalCreationApiResponse.getAccountId().equals(accountId);
                        var savingsGoalCreatedMatches = !savingsGoalCreationApiResponse.isSavingsGoalCreated();
                        var messageMatches = savingsGoalCreationApiResponse.getMessage().equals("Savings goal with name [Car] already exists for account [" + accountId + "] so no additional savings goal was created");
                        var itemsMatches = savingsGoalCreationApiResponse.getCurrentSavingsGoals().getSavingsGoalList().equals(savingsGoalV2List);
                        var sizeMatches = savingsGoalCreationApiResponse.getSavingsGoalsCount() == 2;
                        return accountIdMatches && savingsGoalCreatedMatches && messageMatches && itemsMatches && sizeMatches;
                    })
                    .verifyComplete();
    }

    private Accounts generateAccountsResponse() {
        UUID accountId1 = UUID.randomUUID();
        UUID defaultCategory1 = UUID.randomUUID();
        AccountV2 account1 = new AccountV2();
        account1.setAccountType("some-type");
        account1.setAccountUid(accountId1);
        account1.setCreatedAt(OffsetDateTime.now());
        account1.setName("some-name");
        account1.setCurrency("GBP");
        account1.setDefaultCategory(defaultCategory1);
        UUID accountId2 = UUID.randomUUID();
        UUID defaultCategory2 = UUID.randomUUID();
        AccountV2 account2 = new AccountV2();
        account2.setAccountType("some-type");
        account2.setAccountUid(accountId2);
        account2.setCreatedAt(OffsetDateTime.now());
        account2.setName("some-name");
        account2.setCurrency("GBP");
        account2.setDefaultCategory(defaultCategory2);
        Accounts accountsResponse = new Accounts();
        accountsResponse.setAccounts(List.of(account1, account2));
        return accountsResponse;
    }

    private FeedItems generateTransactionsResponse() {
        FeedItem transaction1 = new FeedItem();
        CurrencyAndAmount amount1 = new CurrencyAndAmount("GBP", Long.parseLong("1213"));
        transaction1.setAmount(amount1);
        transaction1.setDirection("OUT");
        transaction1.setSettlementTime(OffsetDateTime.now().minusDays(100));
        FeedItem transaction2 = new FeedItem();
        CurrencyAndAmount amount2 = new CurrencyAndAmount("GBP", Long.parseLong("432"));
        amount2.setCurrency("GBP");
        amount2.setMinorUnits(Long.parseLong("1213"));
        transaction2.setAmount(amount2);
        transaction2.setDirection("OUT");
        transaction2.setSettlementTime(OffsetDateTime.now().minusDays(100));
        FeedItems transactionsResponse = new FeedItems();
        transactionsResponse.setFeedItems(List.of(transaction1, transaction2));
        return transactionsResponse;
    }
}
