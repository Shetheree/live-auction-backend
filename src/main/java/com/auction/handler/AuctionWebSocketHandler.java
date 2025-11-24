package com.auction.handler;

import com.auction.model.Auction;
import com.auction.model.Bid;
import com.auction.repository.AuctionRepository;
import com.auction.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AuctionWebSocketHandler {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/bid")
    @SendTo("/topic/auction/{auctionId}")
    public Map<String, Object> handleBid(Map<String, Object> bidData) {
        String auctionId = (String) bidData.get("auctionId");
        String bidderUsername = (String) bidData.get("bidderUsername");
        Double amount = ((Number) bidData.get("amount")).doubleValue();

        Auction auction = auctionRepository.findById(auctionId).orElse(null);
        
        if (auction == null || !auction.isLive()) {
            return createErrorResponse("Auction not found or not live");
        }

        if (amount <= auction.getCurrentPrice()) {
            return createErrorResponse("Bid must be higher than current price");
        }

        // Update auction
        auction.setCurrentPrice(amount);
        auction.setCurrentBidder(bidderUsername);
        auctionRepository.save(auction);

        // Save bid
        Bid bid = new Bid();
        bid.setAuctionId(auctionId);
        bid.setBidderUsername(bidderUsername);
        bid.setAmount(amount);
        bid.setTimestamp(LocalDateTime.now());
        bidRepository.save(bid);

        // Broadcast update to all subscribers
        Map<String, Object> response = new HashMap<>();
        response.put("type", "bid_update");
        response.put("auctionId", auctionId);
        response.put("currentPrice", amount);
        response.put("currentBidder", bidderUsername);
        response.put("bidderUsername", bidderUsername);
        response.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/auction/" + auctionId, response);
        
        return response;
    }

    @MessageMapping("/join")
    public void handleJoin(Map<String, Object> joinData) {
        String auctionId = (String) joinData.get("auctionId");
        Auction auction = auctionRepository.findById(auctionId).orElse(null);
        
        if (auction != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("type", "auction_state");
            response.put("auction", auction);
            messagingTemplate.convertAndSend("/topic/auction/" + auctionId, response);
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "error");
        response.put("message", message);
        return response;
    }
}

