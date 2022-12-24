package com.starling.savingsgoalcreator.service;

import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.starling.savingsgoalcreator.clientmodels.v2.Accounts;
import com.starling.savingsgoalcreator.clientmodels.v2.CreateOrUpdateSavingsGoalResponseV2;
import com.starling.savingsgoalcreator.clientmodels.v2.CurrencyAndAmount;
import com.starling.savingsgoalcreator.clientmodels.v2.FeedItems;
import com.starling.savingsgoalcreator.clientmodels.v2.SavingsGoalRequestV2;
import com.starling.savingsgoalcreator.clientmodels.v2.SavingsGoalTransferResponseV2;
import com.starling.savingsgoalcreator.clientmodels.v2.SavingsGoalsV2;
import com.starling.savingsgoalcreator.clientmodels.v2.TopUpRequestV2;
import com.starling.savingsgoalcreator.interceptor.ClientRequestInterceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class StarlingApiRequestService {

    private final ClientRequestInterceptor clientRequestInterceptor;
    private final RestTemplate restTemplate;

    @Value("${starling.base-url}")
    private String starlingBaseUrl;

    public ResponseEntity<Accounts> getAllAccounts() {
        String token = clientRequestInterceptor.getBearerToken().getToken();
        String url = starlingBaseUrl + "/accounts";
        log.info(url);
        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + token);

        // build request
        HttpEntity request = new HttpEntity(headers);

        // make HTTP call
        return restTemplate.exchange(url, HttpMethod.GET, request, Accounts.class, 1);
    }

    public ResponseEntity<FeedItems> getAccountTransactions(UUID accountId, String minDate, String maxDate) {
        String token = clientRequestInterceptor.getBearerToken().getToken();
        String minParam = "minTransactionTimestamp";
        String maxParam = "maxTransactionTimestamp";
        String url = starlingBaseUrl + "/feed/account/" + accountId + "/settled-transactions-between/";
        String finalUrl = UriComponentsBuilder.fromUriString(url)
                                                           .queryParam(minParam, minDate)
                                                           .queryParam(maxParam, maxDate)
                                                           .build()
                                                           .toUriString();
        log.info(finalUrl);
        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + token);

        // build request
        HttpEntity request = new HttpEntity(headers);

        // make HTTP call
        return restTemplate.exchange(finalUrl, HttpMethod.GET, request, FeedItems.class, 1);
    }

    public ResponseEntity<CreateOrUpdateSavingsGoalResponseV2> createSavingsGoal(UUID accountId, String savingsGoalName) {
        String token = clientRequestInterceptor.getBearerToken().getToken();
        String url = starlingBaseUrl + "/account/" + accountId + "/savings-goals";
        String finalUrl = UriComponentsBuilder.fromUriString(url)
                                              .build()
                                              .toUriString();
        log.info(finalUrl);
        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);

        SavingsGoalRequestV2 requestBody = new SavingsGoalRequestV2(savingsGoalName, "GBP");
        // build request
        HttpEntity<SavingsGoalRequestV2> request = new HttpEntity<>(requestBody, headers);

        // make HTTP call
        return restTemplate.exchange(finalUrl, HttpMethod.PUT, request, CreateOrUpdateSavingsGoalResponseV2.class, 1);
    }

    public ResponseEntity<SavingsGoalTransferResponseV2> addMoneyIntoSavingsGoal(UUID accountId, UUID savingsGoalId, long amount) {
        UUID transferId = UUID.randomUUID();
        String token = clientRequestInterceptor.getBearerToken().getToken();
        String url = starlingBaseUrl + "/account/" + accountId + "/savings-goals/" + savingsGoalId + "/add-money/" + transferId;
        String finalUrl = UriComponentsBuilder.fromUriString(url)
                                              .build()
                                              .toUriString();
        log.info(finalUrl);
        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + token);

        TopUpRequestV2 requestBody = new TopUpRequestV2(new CurrencyAndAmount("GBP", amount));
        // build request
        HttpEntity<TopUpRequestV2> request = new HttpEntity<>(requestBody, headers);

        // make HTTP call
        return restTemplate.exchange(finalUrl, HttpMethod.PUT, request, SavingsGoalTransferResponseV2.class, 1);
    }

    public ResponseEntity<SavingsGoalsV2> getAllSavingsGoals(UUID accountId) {
        String token = clientRequestInterceptor.getBearerToken().getToken();
        String url = starlingBaseUrl + "/account/" + accountId + "/savings-goals";
        String finalUrl = UriComponentsBuilder.fromUriString(url)
                                              .build()
                                              .toUriString();
        log.info(finalUrl);
        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + token);

        // build request
        HttpEntity request = new HttpEntity(headers);

        // make HTTP call
        return restTemplate.exchange(finalUrl, HttpMethod.GET, request, SavingsGoalsV2.class, 1);
    }
}
