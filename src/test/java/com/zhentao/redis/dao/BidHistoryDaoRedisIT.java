package com.zhentao.redis.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import redis.embedded.RedisServer;

import com.zhentao.redis.config.RedisConfig;
import com.zhentao.redis.dao.BidHistoryDao;
import com.zhentao.redis.model.Bid;
import com.zhentao.redis.model.BidHistory;

@ContextConfiguration(classes = { RedisConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class BidHistoryDaoRedisIT {
    @Inject
    private BidHistoryDao bidHistoryDaoRedis;

    @Value("${redis.port}")
    int port;

    private RedisServer redisServer;

    @Before
    public void setup() throws Exception {
        redisServer = new RedisServer(port);
        redisServer.start();
    }

    @After
    public void tearDown() throws InterruptedException {
        redisServer.stop();
    }

    @Test
    public void testSingleUpdate() {
        String uid = "uid";
        String brand = "brand";
        double price = 100.5;
        bidHistoryDaoRedis.update(uid, brand, price);
        Map<String, BidHistory> data = bidHistoryDaoRedis.getBidHistory(uid);
        assertThat(data.get(brand).getCount(), is(1));
        assertThat(data.get(brand).getMax(), is(price));
        assertThat(data.get(brand).getSum(), is(price));
    }

    @Test
    public void testBatchUpdate() {
        String uid = "uid";
        String brand1 = "b1";
        double price1 = 90.1;
        List<Bid> list = new ArrayList<>();
        list.add(new Bid(brand1, price1));

        String brand2 = "b2";
        double price2 = 100.2;
        list.add(new Bid(brand2, price2));

        bidHistoryDaoRedis.update(uid, list);

        Map<String, BidHistory> data = bidHistoryDaoRedis.getBidHistory(uid);
        assertThat(data.size(), is(2));
        assertThat(data.get(brand1).getCount(), is(1));
        assertThat(data.get(brand2).getMax(), is(price2));
        assertThat(data.get(brand1).getSum(), is(price1));

        list = new ArrayList<>();
        double price3 = 99.5;
        list.add(new Bid(brand1, price3));
        bidHistoryDaoRedis.update(uid, list);
        data = bidHistoryDaoRedis.getBidHistory(uid);

        assertThat(data.size(), is(2));
        assertThat(data.get(brand1).getCount(), is(2));
        assertThat(data.get(brand1).getMax(), is(price3));
        assertThat(data.get(brand1).getSum(), is(price1 + price3));
        assertThat(data.get(brand2).getMax(), is(price2));
    }
}
