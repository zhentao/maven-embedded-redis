package com.zhentao.redis.dao;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.zhentao.redis.model.Bid;

@RunWith(MockitoJUnitRunner.class)
public class BidHistoryDaoRedisTest {
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private BoundHashOperations<String, String, String> hashOperations;
    @Mock
    private Map<String, String> exist;

    private BidHistoryDaoRedis uviDaoRedis;
    private final int timeout = 10000;
    @Before
    public void setup() {
        uviDaoRedis = new BidHistoryDaoRedis(redisTemplate, timeout);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateList() {
        String uid = "uid";
        List<Bid> bidRecordList = new ArrayList<>();
        bidRecordList.add(new Bid("zz", 50));
        when(redisTemplate.<String, String>boundHashOps(uid)).thenReturn(hashOperations);
        uviDaoRedis.update(uid, bidRecordList);
        verify(redisTemplate).execute(any(SessionCallback.class));
    }

    @Test
    public void testExpire() {
        String uid = "uid";
        when(redisTemplate.getExpire(uid)).thenReturn(-1L);
        uviDaoRedis.expire(uid);
        verify(redisTemplate).expire(uid, timeout, TimeUnit.SECONDS);
    }

    @Test
    public void testCalculateStatsWithKeyExisting() {
        String uid = "uid";
        List<Bid> bidRecordList = new ArrayList<>();
        bidRecordList.add(new Bid("zz", 50));
        bidRecordList.add(new Bid("yy", 0.5));


        when(exist.containsKey(any(String.class))).thenReturn(true);
        when(exist.get(any(String.class))).thenReturn("1");

        uviDaoRedis.calculateStats(uid, bidRecordList, exist);
        verify(exist, times(5)).put(any(String.class), any(String.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSingleUpdate() {
        uviDaoRedis.update("uid", "xyz", 100);
        verify(redisTemplate).execute(any(SessionCallback.class));
    }

    @Test
    public void testGetBidHistory() {
        String uid = "uid";
        Map<String, String> fromRedis = new HashMap<String ,String>();
        fromRedis.put("b1" + BidHistoryDaoRedis.KEY_COUNT, "1");
        fromRedis.put("b1" + BidHistoryDaoRedis.KEY_MAX, "11.5");
        fromRedis.put("b1" + BidHistoryDaoRedis.KEY_SUM, "11.5");


        when(redisTemplate.<String, String>boundHashOps(uid)).thenReturn(hashOperations);
        when(hashOperations.entries()).thenReturn(fromRedis);
        uviDaoRedis.getBidHistory(uid);
        verify(redisTemplate).boundHashOps(uid);
    }
}
