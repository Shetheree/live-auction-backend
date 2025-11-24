package com.auction.controller;

import com.auction.model.Auction;
import com.auction.model.Bid;
import com.auction.model.User;
import com.auction.repository.AuctionRepository;
import com.auction.repository.BidRepository;
import com.auction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuctionController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    // Authentication endpoints
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User registered successfully", "user", savedUser));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("user", user.get());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
    }

    // Auction endpoints
    @GetMapping("/auctions")
    public ResponseEntity<List<Auction>> getAllAuctions() {
        return ResponseEntity.ok(auctionRepository.findAll());
    }

    @GetMapping("/auctions/live")
    public ResponseEntity<List<Auction>> getLiveAuctions() {
        return ResponseEntity.ok(auctionRepository.findByLive(true));
    }

    @GetMapping("/auctions/{id}")
    public ResponseEntity<Auction> getAuction(@PathVariable String id) {
        return auctionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/auctions")
    public ResponseEntity<Auction> createAuction(@RequestBody Auction auction) {
        auction.setStartTime(LocalDateTime.now());
        auction.setStatus("UPCOMING");
        auction.setLive(false);
        auction.setCurrentPrice(auction.getStartingPrice());
        Auction savedAuction = auctionRepository.save(auction);
        return ResponseEntity.ok(savedAuction);
    }

    @PostMapping("/auctions/{id}/start")
    public ResponseEntity<?> startAuction(@PathVariable String id) {
        Optional<Auction> auctionOpt = auctionRepository.findById(id);
        if (auctionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Auction auction = auctionOpt.get();
        auction.setLive(true);
        auction.setStatus("LIVE");
        auction.setStartTime(LocalDateTime.now());
        auctionRepository.save(auction);
        return ResponseEntity.ok(auction);
    }

    @PostMapping("/auctions/{id}/end")
    public ResponseEntity<?> endAuction(@PathVariable String id) {
        Optional<Auction> auctionOpt = auctionRepository.findById(id);
        if (auctionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Auction auction = auctionOpt.get();
        auction.setLive(false);
        auction.setStatus("ENDED");
        auction.setEndTime(LocalDateTime.now());
        auctionRepository.save(auction);
        return ResponseEntity.ok(auction);
    }

    // Bid endpoints
    @GetMapping("/auctions/{id}/bids")
    public ResponseEntity<List<Bid>> getBids(@PathVariable String id) {
        return ResponseEntity.ok(bidRepository.findByAuctionIdOrderByTimestampDesc(id));
    }

    @PostMapping("/auctions/{id}/bids")
    public ResponseEntity<?> placeBid(@PathVariable String id, @RequestBody Map<String, Object> bidData) {
        Optional<Auction> auctionOpt = auctionRepository.findById(id);
        if (auctionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Auction auction = auctionOpt.get();
        if (!auction.isLive()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Auction is not live"));
        }

        String bidderUsername = (String) bidData.get("bidderUsername");
        Double amount = ((Number) bidData.get("amount")).doubleValue();

        if (amount <= auction.getCurrentPrice()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Bid must be higher than current price"));
        }

        auction.setCurrentPrice(amount);
        auction.setCurrentBidder(bidderUsername);
        auctionRepository.save(auction);

        Bid bid = new Bid();
        bid.setAuctionId(id);
        bid.setBidderUsername(bidderUsername);
        bid.setAmount(amount);
        bid.setTimestamp(LocalDateTime.now());
        bidRepository.save(bid);

        return ResponseEntity.ok(bid);
    }
}

