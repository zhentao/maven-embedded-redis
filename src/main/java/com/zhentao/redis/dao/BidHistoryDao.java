package com.zhentao.redis.dao;

import java.util.List;
import java.util.Map;

import com.zhentao.redis.model.Bid;
import com.zhentao.redis.model.BidHistory;

public interface BidHistoryDao {
    /**
     * Batch update a list of bid record
     *
     * @param uid
     * @param bidRecordList
     * @return true for succeeding to update the record
     */
    boolean update(String uid, List<Bid> bids);

    /**
     * update a single record
     *
     * @param uid
     * @param brandId
     * @param price
     * @return true for succeeding to update the record
     */
    boolean update(String uid, String brandId, double price);

    /**
     * get a bid history summary of user
     *
     * @param uid
     * @return a map for bid history
     */
    Map<String, BidHistory> getBidHistory(String uid);

}