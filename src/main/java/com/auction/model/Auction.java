package com.auction.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "auctions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auction {
    @Id
    private String id;
    private String title;
    private String description;
    private String createdBy; // username
    private double startingPrice;
    private double currentPrice;
    private String currentBidder; // username of highest bidder
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @JsonProperty("isLive")
    private boolean live;
    private String status; // "UPCOMING", "LIVE", "ENDED"
}

