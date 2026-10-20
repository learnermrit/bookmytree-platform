package com.bookmytree.platform.inventory.service;

import com.bookmytree.platform.common.exception.ConcurrencyLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchLockService {

    private final RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "lock:branch:";

    /**
     * Attempts to acquire a distributed lock for a specific tree branch during checkout.
     *
     * @param branchId The UUID of the tree branch being booked
     * @param waitTime Time to wait to acquire the lock (in seconds)
     * @param leaseTime Time after which the lock is automatically released (in seconds)
     * @return The acquired RLock instance
     */
    public RLock acquireBranchLock(String branchId, long waitTime, long leaseTime) {
        String lockKey = LOCK_PREFIX + branchId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            log.debug("Attempting to acquire distributed lock for branch ID: {}", branchId);
            boolean isLocked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            
            if (!isLocked) {
                log.warn("Concurrency conflict: Branch {} is currently locked by another transaction.", branchId);
                throw new ConcurrencyLockException("This tree branch is currently being booked by another user. Please try again shortly.");
            }
            
            log.info("Successfully acquired distributed lock for branch ID: {}", branchId);
            return lock;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted while waiting for branch lock: {}", branchId, e);
            throw new ConcurrencyLockException("Booking request was interrupted due to high concurrency.");
        }
    }

    /**
     * Safely releases the distributed lock.
     */
    public void releaseLock(RLock lock, String branchId) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            try {
                lock.unlock();
                log.info("Released distributed lock for branch ID: {}", branchId);
            } catch (IllegalMonitorStateException e) {
                log.error("Attempted to unlock a lock not held by current thread for branch: {}", branchId, e);
            }
        }
    }
}