package com.starling.savingsgoalcreator.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.format.datetime.standard.DateTimeFormatterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.starling.savingsgoalcreator.clientmodels.v2.AccountV2;
import com.starling.savingsgoalcreator.clientmodels.v2.Accounts;
import com.starling.savingsgoalcreator.clientmodels.v2.CreateOrUpdateSavingsGoalResponseV2;
import com.starling.savingsgoalcreator.clientmodels.v2.CurrencyAndAmount;
import com.starling.savingsgoalcreator.clientmodels.v2.FeedItem;
import com.starling.savingsgoalcreator.clientmodels.v2.FeedItems;
import com.starling.savingsgoalcreator.clientmodels.v2.SavingsGoalRequestV2;
import com.starling.savingsgoalcreator.clientmodels.v2.SavingsGoalTransferResponseV2;
import com.starling.savingsgoalcreator.clientmodels.v2.SavingsGoalV2;
import com.starling.savingsgoalcreator.clientmodels.v2.SavingsGoalsV2;
import com.starling.savingsgoalcreator.clientmodels.v2.TopUpRequestV2;
import com.starling.savingsgoalcreator.model.SavingsGoalCreationApiRequestBody;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
class StarlingApiRequestServiceTest {

    @Mock
    private WebClient webClientMock;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpecMock;
    @Mock
    private WebClient.RequestBodySpec requestBodySpecMock;
    @Mock
    private WebClient.ResponseSpec responseSpecMock;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpecMock;

    @InjectMocks
    private StarlingApiRequestService service;

    private final String TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN);

    @Test
    void test_getAllAccounts() {
        Accounts accountsResponse = generateAccountsResponse();

        when(webClientMock.method(HttpMethod.GET)).thenReturn(requestBodyUriSpecMock);
        when(requestBodyUriSpecMock.uri("/accounts")).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(Accounts.class)).thenReturn(Mono.just(accountsResponse));

        Mono<Accounts> result = service.getAllAccounts();

        StepVerifier.create(result)
                    .expectNextMatches(accounts -> accounts.getAccounts().equals(accountsResponse.getAccounts()))
                    .verifyComplete();

        verify(webClientMock, times(1)).method(HttpMethod.GET);
        verify(requestBodyUriSpecMock, times(1)).uri("/accounts");
        verify(requestBodySpecMock, times(1)).retrieve();
        verify(responseSpecMock, times(1)).bodyToMono(Accounts.class);
    }

    @Test
    void test_getAccountTransactions() {
        UUID accountId = UUID.randomUUID();
        String minDate = dateTimeFormatter.format(LocalDateTime.now().minusDays(100));
        String maxDate = dateTimeFormatter.format(LocalDateTime.now());
        FeedItems transactionsResponse = generateTransactionsResponse();

        when(webClientMock.method(HttpMethod.GET)).thenReturn(requestBodyUriSpecMock);
        when(requestBodyUriSpecMock.uri("/feed/account/" + accountId + "/settled-transactions-between?" + "minTransactionTimestamp=" + minDate + "&maxTransactionTimestamp=" + maxDate)).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(FeedItems.class)).thenReturn(Mono.just(transactionsResponse));

        Mono<FeedItems> result = service.getAccountTransactions(accountId, minDate, maxDate);

        StepVerifier.create(result)
                    .expectNextMatches(feedItems -> feedItems.getFeedItems().equals(transactionsResponse.getFeedItems()))
                    .verifyComplete();

        verify(webClientMock, times(1)).method(HttpMethod.GET);
        verify(requestBodyUriSpecMock, times(1)).uri("/feed/account/" + accountId + "/settled-transactions-between?" + "minTransactionTimestamp=" + minDate + "&maxTransactionTimestamp=" + maxDate);
        verify(requestBodySpecMock, times(1)).retrieve();
        verify(responseSpecMock, times(1)).bodyToMono(FeedItems.class);
    }

    @Test
    void test_createSavingsGoal() {
        UUID accountId = UUID.randomUUID();
        UUID savingsGoalUuid = UUID.randomUUID();
        SavingsGoalCreationApiRequestBody requestBody = new SavingsGoalCreationApiRequestBody();
        requestBody.setCurrency("GBP");
        requestBody.setSavingsGoalName("Car");
        CreateOrUpdateSavingsGoalResponseV2 response = new CreateOrUpdateSavingsGoalResponseV2();
        response.setSavingsGoalUid(savingsGoalUuid);
        response.setErrors(Collections.emptyList());
        response.setSuccess(true);

        when(webClientMock.method(HttpMethod.PUT)).thenReturn(requestBodyUriSpecMock);
        when(requestBodyUriSpecMock.uri("/account/" + accountId + "/savings-goals")).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.bodyValue(any(SavingsGoalRequestV2.class))).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(CreateOrUpdateSavingsGoalResponseV2.class)).thenReturn(Mono.just(response));

        Mono<CreateOrUpdateSavingsGoalResponseV2> result = service.createSavingsGoal(accountId, requestBody);

        StepVerifier.create(result)
                    .expectNextMatches(savingsGoalResponseV2 -> savingsGoalResponseV2.getSavingsGoalUid().equals(savingsGoalUuid))
                    .verifyComplete();

        verify(webClientMock, times(1)).method(HttpMethod.PUT);
        verify(requestBodyUriSpecMock, times(1)).uri("/account/" + accountId + "/savings-goals");
        verify(requestBodySpecMock, times(1)).bodyValue(any(SavingsGoalRequestV2.class));
        verify(requestHeadersSpecMock, times(1)).retrieve();
        verify(responseSpecMock, times(1)).bodyToMono(CreateOrUpdateSavingsGoalResponseV2.class);
    }

    @Test
    void test_addMoneyIntoSavingsGoal() {
        UUID accountId = UUID.randomUUID();
        UUID savingsGoalUuid = UUID.randomUUID();
        UUID savingsGoalTransferUuid = UUID.randomUUID();
        SavingsGoalCreationApiRequestBody requestBody = new SavingsGoalCreationApiRequestBody();
        requestBody.setCurrency("GBP");
        requestBody.setSavingsGoalName("Car");
        SavingsGoalTransferResponseV2 response = new SavingsGoalTransferResponseV2();
        response.setTransferUid(savingsGoalTransferUuid);
        response.setErrors(Collections.emptyList());
        response.setSuccess(true);

        when(webClientMock.method(HttpMethod.PUT)).thenReturn(requestBodyUriSpecMock);
        when(requestBodyUriSpecMock.uri(anyString())).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.bodyValue(any(TopUpRequestV2.class))).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(SavingsGoalTransferResponseV2.class)).thenReturn(Mono.just(response));

        Mono<SavingsGoalTransferResponseV2> result = service.addMoneyIntoSavingsGoal(accountId, savingsGoalUuid, "GBP", Long.parseLong("123"));

        StepVerifier.create(result)
                    .expectNextMatches(transferResponse -> transferResponse.getErrors().isEmpty())
                    .verifyComplete();

        verify(webClientMock, times(1)).method(HttpMethod.PUT);
        verify(requestBodyUriSpecMock, times(1)).uri(anyString());
        verify(requestBodySpecMock, times(1)).bodyValue(any(TopUpRequestV2.class));
        verify(requestHeadersSpecMock, times(1)).retrieve();
        verify(responseSpecMock, times(1)).bodyToMono(SavingsGoalTransferResponseV2.class);
    }

    @Test
    void test_getAllSavingsGoals() {
        UUID accountId = UUID.randomUUID();
        UUID savingsGoalUuid1 = UUID.randomUUID();
        UUID savingsGoalUuid2 = UUID.randomUUID();
        SavingsGoalCreationApiRequestBody requestBody = new SavingsGoalCreationApiRequestBody();
        requestBody.setCurrency("GBP");
        requestBody.setSavingsGoalName("Car");
        SavingsGoalV2 savingsGoal1 = new SavingsGoalV2();
        savingsGoal1.setSavingsGoalUid(savingsGoalUuid1);
        savingsGoal1.setName("Car");
        SavingsGoalV2 savingsGoal2 = new SavingsGoalV2();
        savingsGoal2.setSavingsGoalUid(savingsGoalUuid2);
        savingsGoal2.setName("House");
        SavingsGoalsV2 response = new SavingsGoalsV2();
        List<SavingsGoalV2> savingsGoalV2List = List.of(savingsGoal1, savingsGoal2);
        response.setSavingsGoalList(savingsGoalV2List);

        when(webClientMock.method(HttpMethod.GET)).thenReturn(requestBodyUriSpecMock);
        when(requestBodyUriSpecMock.uri("/account/" + accountId + "/savings-goals")).thenReturn(requestBodySpecMock);
        when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(SavingsGoalsV2.class)).thenReturn(Mono.just(response));

        Mono<SavingsGoalsV2> result = service.getAllSavingsGoals(accountId);

        StepVerifier.create(result)
                    .expectNextMatches(savingsGoalsV2 -> savingsGoalsV2.getSavingsGoalList().equals(savingsGoalV2List))
                    .verifyComplete();

        verify(webClientMock, times(1)).method(HttpMethod.GET);
        verify(requestBodyUriSpecMock, times(1)).uri("/account/" + accountId + "/savings-goals");
        verify(requestBodySpecMock, times(1)).retrieve();
        verify(responseSpecMock, times(1)).bodyToMono(SavingsGoalsV2.class);
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
