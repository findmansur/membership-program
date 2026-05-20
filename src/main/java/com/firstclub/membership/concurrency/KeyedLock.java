package com.firstclub.membership.concurrency;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * A lightweight per-key mutex implemented as a striped lock map.
 *
 * <p>Subscription operations (subscribe / upgrade / downgrade / cancel) must
 * be serialised per user, otherwise two concurrent requests on the same user
 * could create duplicate active subscriptions or race over tier transitions.
 * Locking the whole table would serialise unrelated users; using {@code @Version}
 * alone forces clients to retry on every collision. A per-user lock keyed by
 * userId gives the best of both worlds.
 *
 * <p>Locks are reference-counted so unused entries are removed from the map,
 * preventing unbounded memory growth on a long-lived process.
 *
 * @param <K> the type of the lock key (typically a userId)
 */
@Component
public class KeyedLock<K> {

    private static final class CountedLock {
        final ReentrantLock lock = new ReentrantLock();
        final AtomicInteger refCount = new AtomicInteger(0);
    }

    private final ConcurrentHashMap<K, CountedLock> locks = new ConcurrentHashMap<>();

    /**
     * Acquires the lock for {@code key}, runs the supplier, and releases the
     * lock — even if the supplier throws.
     */
    public <T> T executeLocked(K key, Supplier<T> action) {
        CountedLock cl = acquire(key);
        try {
            cl.lock.lock();
            try {
                return action.get();
            } finally {
                cl.lock.unlock();
            }
        } finally {
            release(key, cl);
        }
    }

    public void executeLocked(K key, Runnable action) {
        executeLocked(key, () -> {
            action.run();
            return null;
        });
    }

    private CountedLock acquire(K key) {
        return locks.compute(key, (k, existing) -> {
            CountedLock cl = (existing == null) ? new CountedLock() : existing;
            cl.refCount.incrementAndGet();
            return cl;
        });
    }

    private void release(K key, CountedLock cl) {
        locks.computeIfPresent(key, (k, current) -> {
            if (current != cl) {
                return current;
            }
            return current.refCount.decrementAndGet() == 0 ? null : current;
        });
    }

    /** Visible for testing — number of currently-tracked keys. */
    public int trackedKeyCount() {
        return locks.size();
    }
}
