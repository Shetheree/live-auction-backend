package com.auction.repository;

import com.auction.model.Bid;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends MongoRepository<Bid, String> {
    List<Bid> findByAuctionIdOrderByTimestampDesc(String auctionId);
    List<Bid> findByAuctionIdAndBidderUsername(String auctionId, String bidderUsername);
}

