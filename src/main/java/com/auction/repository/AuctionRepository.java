package com.auction.repository;

import com.auction.model.Auction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionRepository extends MongoRepository<Auction, String> {
    List<Auction> findByStatus(String status);
    List<Auction> findByLive(boolean live);
}

