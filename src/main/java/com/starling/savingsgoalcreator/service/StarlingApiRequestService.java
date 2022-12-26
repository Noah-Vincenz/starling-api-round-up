package com.starling.savingsgoalcreator.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${starling.base-url}")
    private String starlingBaseUrl;

    public Mono<Accounts> getAllAccounts() {
        // make HTTP call
        WebClient.RequestBodySpec uriSpec = webClient.method(HttpMethod.GET)
                                                     .uri("/accounts");
        return uriSpec.retrieve()
                      .bodyToMono(Accounts.class);
    }

    public Mono<FeedItems> getAccountTransactions(UUID accountId, String minDate, String maxDate) {
        String minParam = "minTransactionTimestamp";
        String maxParam = "maxTransactionTimestamp";
        String url = starlingBaseUrl + "/feed/account/" + accountId + "/settled-transactions-between/";
        String finalUrl = UriComponentsBuilder.fromUriString(url)
                                                           .queryParam(minParam, minDate)
                                                           .queryParam(maxParam, maxDate)
                                                           .build()
                                                           .toUriString();
        log.info(finalUrl);

        WebClient.RequestBodySpec uriSpec = webClient.method(HttpMethod.GET)
                                                     .uri(finalUrl);
        return uriSpec.retrieve()
                      .bodyToMono(FeedItems.class);
    }

    public Mono<CreateOrUpdateSavingsGoalResponseV2> createSavingsGoal(UUID accountId, String savingsGoalName) {

        SavingsGoalRequestV2 requestBody = new SavingsGoalRequestV2(savingsGoalName, "GBP");
        WebClient.RequestHeadersSpec uriSpec = webClient.method(HttpMethod.PUT)
                                                        .uri("/account/" + accountId + "/savings-goals")
                                                        .bodyValue(requestBody);
        return uriSpec.retrieve()
                      .bodyToMono(CreateOrUpdateSavingsGoalResponseV2.class);
    }

    public Mono<SavingsGoalTransferResponseV2> addMoneyIntoSavingsGoal(UUID accountId, UUID savingsGoalId, long amount) {
        UUID transferId = UUID.randomUUID();
        TopUpRequestV2 requestBody = new TopUpRequestV2(new CurrencyAndAmount("GBP", amount));

        // make HTTP call
        WebClient.RequestHeadersSpec uriSpec = webClient.method(HttpMethod.PUT)
                                                        .uri("/account/" + accountId + "/savings-goals/" + savingsGoalId + "/add-money/" + transferId)
                                                        .bodyValue(requestBody);
        return uriSpec.retrieve()
                      .bodyToMono(SavingsGoalTransferResponseV2.class);
    }

    public Mono<SavingsGoalsV2> getAllSavingsGoals(UUID accountId) {
        // make HTTP call
        WebClient.RequestBodySpec uriSpec = webClient.method(HttpMethod.GET)
                                                     .uri("/account/" + accountId + "/savings-goals");
        return uriSpec.retrieve()
                      .bodyToMono(SavingsGoalsV2.class);
    }
}
