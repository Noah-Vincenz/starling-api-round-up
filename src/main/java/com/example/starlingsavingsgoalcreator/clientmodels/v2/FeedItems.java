package com.example.starlingsavingsgoalcreator.clientmodels.v2;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Data
public class FeedItems {
    private List<FeedItem> feedItems;
}
