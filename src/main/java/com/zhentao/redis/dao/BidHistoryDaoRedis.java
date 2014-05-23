package com.zhentao.redis.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.zhentao.redis.model.Bid;
import com.zhentao.redis.model.BidHistory;

public class BidHistoryDaoRedis implements BidHistoryDao {
    public final static String KEY_COUNT = "_C";
    public final static String KEY_SUM = "_S";
    public final static String KEY_MAX = "_M";

    private final int expirationInSeconds;
    private final StringRedisTemplate redisTemplate;

    final static Logger log = LoggerFactory.getLogger(BidHistoryDaoRedis.class);

    public BidHistoryDaoRedis(StringRedisTemplate redisTemplate, int expirationInSeconds) {
        this.redisTemplate = redisTemplate;
        this.expirationInSeconds = expirationInSeconds;
    }

    @Override
    public boolean update(final String uid, final List<Bid> bids) {
        @SuppressWarnings("unchecked")
        List<Object> results = (List<Object>) redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            @SuppressWarnings({ "rawtypes" })
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.watch(uid);
                try {
                    Map<String, String> exist = calculateStats(uid, bids, redisTemplate.<String, String> opsForHash()
                                                    .entries(uid));
                    operations.multi();
                    BoundHashOperations<String, Object, Object> boundHashOps = operations.boundHashOps(uid);
                    boundHashOps.putAll(exist);
                    return operations.exec();
                } finally {
                    operations.unwatch();
                }
            }
        });
        expire(uid);
        return results == null ? false : true;
    }

    void expire(final String uid) {
        if (redisTemplate.getExpire(uid) == -1) {
            redisTemplate.expire(uid, expirationInSeconds, TimeUnit.SECONDS);
        }
    }

    Map<String, String> calculateStats(final String uid, List<Bid> bids, Map<String, String> exist) {
        if (exist == null) {
            exist = new HashMap<String, String>();
        }
        for (Bid bidRecord : bids) {
            String keyCount = bidRecord.getBrand() + KEY_COUNT;
            String keySum = bidRecord.getBrand() + KEY_SUM;
            String keyMax = bidRecord.getBrand() + KEY_MAX;
            if (exist.containsKey(keyCount)) {
                exist.put(keyCount, String.valueOf(Integer.parseInt(exist.get(keyCount)) + 1));
            } else {
                exist.put(keyCount, "1");
            }
            if (exist.containsKey(keySum)) {
                exist.put(keySum, String.valueOf(Double.parseDouble(exist.get(keySum)) + bidRecord.getPrice()));
            } else {
                exist.put(keySum, String.valueOf(bidRecord.getPrice()));
            }
            if (!exist.containsKey(keyMax) || Double.parseDouble(exist.get(keyMax)) < bidRecord.getPrice()) {
                exist.put(keyMax, String.valueOf(bidRecord.getPrice()));
            }
        }

        return exist;
    }

    @Override
    public boolean update(final String uid, final String brandId, final double price) {
        @SuppressWarnings("unchecked")
        List<Object> results = (List<Object>) redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.watch(uid);
                try {
                    final String brandMaxKey = brandId + KEY_MAX;
                    String max = (String) redisTemplate.opsForHash().get(uid, brandMaxKey);
                    operations.multi();
                    BoundHashOperations boundHashOps = operations.boundHashOps(uid);

                    if (max == null || Double.parseDouble(max) < price) {
                        boundHashOps.put(brandMaxKey, String.valueOf(price));
                    }

                    boundHashOps.increment(brandId + KEY_COUNT, 1);
                    boundHashOps.increment(brandId + KEY_SUM, price);
                    return operations.exec();
                } finally {
                    operations.unwatch();
                }

            }
        });
        expire(uid);
        return results == null ? false : true;
    }

    @Override
    public Map<String, BidHistory> getBidHistory(String uid) {
        Map<String, String> redis = redisTemplate.<String, String> boundHashOps(uid).entries();
        Map<String, BidHistory> r = new HashMap<String, BidHistory>();
        for (String key : redis.keySet()) {// find all the brand id
            String brand = key.substring(0, key.length() - 2);
            if (r.containsKey(brand)) {
                continue;
            }
            BidHistory b = new BidHistory();
            b.setCount((int) getValue(redis, brand + KEY_COUNT));
            b.setMax(getValue(redis, brand + KEY_MAX));
            b.setSum(getValue(redis, brand + KEY_SUM));

            r.put(brand, b);
        }
        return r;
    }

    private double getValue(Map<String, String> m, String k) {
        String v = m.get(k);
        if (v != null) {
            return Double.parseDouble(v);
        }
        return 0.0;
    }
}
