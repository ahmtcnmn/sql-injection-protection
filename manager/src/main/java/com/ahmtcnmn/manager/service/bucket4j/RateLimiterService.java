package com.ahmtcnmn.manager.service.bucket4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

@Component
public class RateLimiterService {
    
    private final Map<String,Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key, int capacity, Duration refillDuration){
     return buckets.computeIfAbsent(key, k-> createNewBucket(capacity,refillDuration));
    }
    private Bucket createNewBucket(int capacity, Duration refillDurayion){
        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(capacity, refillDurayion));
        return Bucket.builder().addLimit(limit).build();
    }
}
