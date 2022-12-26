package com.starling.savingsgoalcreator.service;

import java.util.UUID;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.starling.savingsgoalcreator.clientmodels.v2.Accounts;
import com.starling.savingsgoalcreator.clientmodels.v2.CreateOrUpdateSavingsGoalResponseV2;
import com.starling.savingsgoalcreator.clientmodels.v2.CurrencyAndAmount;
import com.starling.savingsgoalcreator.clientmodels.v2.FeedItems;
import com.starling.savingsgoalcreator.clientmodels.v2.SavingsGoalRequestV2;
import com.starling.savingsgoalcreator.clientmodels.v2.SavingsGoalTransferResponseV2;
import com.starling.savingsgoalcreator.clientmodels.v2.SavingsGoalsV2;
import com.starling.savingsgoalcreator.clientmodels.v2.TopUpRequestV2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class StarlingApiRequestService {

    private final WebClient webClient;
    private static final String CURRENCY_GBP = "GBP";
    private static final String PARAM_MIN_TX_TIMESTAMP = "minTransactionTimestamp";
    private static final String PARAM_MAX_TX_TIMESTAMP = "maxTransactionTimestamp";

    public Mono<Accounts> getAllAccounts() {
        return webClient.method(HttpMethod.GET)
                        .uri("/accounts")
                        .retrieve()
                        .bodyToMono(Accounts.class);
    }

    public Mono<FeedItems> getAccountTransactions(UUID accountId, String minDate, String maxDate) {
        return webClient.method(HttpMethod.GET)
                        .uri(UriComponentsBuilder.fromUriString("/feed/account/" + accountId + "/settled-transactions-between")
                                                 .queryParam(PARAM_MIN_TX_TIMESTAMP, minDate)
                                                 .queryParam(PARAM_MAX_TX_TIMESTAMP, maxDate)
                                                 .build()
                                                 .toUriString())
                        .retrieve()
                        .bodyToMono(FeedItems.class);
    }

    public Mono<CreateOrUpdateSavingsGoalResponseV2> createSavingsGoal(UUID accountId, String savingsGoalName) {
        return webClient.method(HttpMethod.PUT)
                        .uri("/account/" + accountId + "/savings-goals")
                        .bodyValue(new SavingsGoalRequestV2(savingsGoalName, CURRENCY_GBP))
                        .retrieve()
                        .bodyToMono(CreateOrUpdateSavingsGoalResponseV2.class);
    }

    public Mono<SavingsGoalTransferResponseV2> addMoneyIntoSavingsGoal(UUID accountId, UUID savingsGoalId, long amount) {
        return webClient.method(HttpMethod.PUT)
                        .uri("/account/" + accountId + "/savings-goals/" + savingsGoalId + "/add-money/" + UUID.randomUUID())
                        .bodyValue(new TopUpRequestV2(new CurrencyAndAmount(CURRENCY_GBP, amount)))
                        .retrieve()
                        .bodyToMono(SavingsGoalTransferResponseV2.class);
    }

    public Mono<SavingsGoalsV2> getAllSavingsGoals(UUID accountId) {
        return webClient.method(HttpMethod.GET)
                        .uri("/account/" + accountId + "/savings-goals")
                        .retrieve()
                        .bodyToMono(SavingsGoalsV2.class);
    }
}
