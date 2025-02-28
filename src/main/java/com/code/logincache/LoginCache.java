package com.code.logincache;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Cache implementation.
 */
@Component
public class LoginCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginCache.class);
    // The max cache size
    private static final int MAX_CACHE = 10;
    // I use this FIFO list to keep track on the last user ids added to the cache.
    // This is used to easy remove the first added user id from the cache when the cache needs to be reduced.
    private static final Queue<String> lastLoggedInUserIds = new LinkedList<>();
    // Use HashMap even though it is not thread safe as ConcurrentHashMap or HashTable.
    // In this case I synchronize on LoginCache itself, as we are also using a queue.
    // If no queue was used, I would have used ConcurrentHashMap
    private static final Map<String, LocalDateTime> cache = new HashMap<>(MAX_CACHE);

    /**
     * Adds the specified user id with the specified time to the cache. If the cache has reached its max size, the oldest entry will be removed.
     *
     * @param userId The user id.
     * @param now    current time.
     */
    public final void addToCache(final String userId, final LocalDateTime now) {
        if (StringUtils.isEmpty(userId)) {
            LOGGER.warn("Cannot add empty/null user id or null login time to the cache");
            return;
        }
        if (cache.size() == MAX_CACHE && !cache.containsKey(userId)) {
            validateCache();
        }
        lastLoggedInUserIds.add(userId);
        cache.put(userId, now);
        LOGGER.debug("Added {} to the cache", userId);
    }

    private void validateCache() {
        if (lastLoggedInUserIds.isEmpty()) {
            // If the lastLoggedInUserIds list is empty clear the cache.
            LOGGER.warn("Clearing the cache as lastLoggedInUserIds is empty");
            cache.clear();
        } else {
            final String oldestUserId = lastLoggedInUserIds.poll();
            LOGGER.debug("Removed {} from lastLoggedInUserIds", oldestUserId);
            final boolean removedFromCache = removeFromCache(oldestUserId);
            if (!removedFromCache) {
                // If the user id retrieved from lastLoggedInUserIds (oldestUserId) was not found on the cache, then clear the cache.
                LOGGER.warn("Clearing the cache and lastLoggedInUserIds as {} was not found in cache, but was found in lastLoggedInUserIds ", oldestUserId);
                cache.clear();
                lastLoggedInUserIds.clear();
            }
        }
    }

    private boolean removeFromCache(String id) {
        LocalDateTime removedEntry = cache.remove(id);
        if (removedEntry != null) {
            LOGGER.debug("Removed {} from cache", id);
        }
        return removedEntry != null;
    }

    /**
     * Returns the current size of the cache.
     *
     * @return int The size of the cache.
     */
    public final int getCacheSize() {
        return cache.size();
    }

    /**
     * @param userId The user id.
     * @return boolean If cache contains the user id return true, otherwise false.
     */
    public final boolean isUserInCache(final String userId) {
        return cache.containsKey(userId);
    }

    /**
     * Returns the last login for the specified user id.
     *
     * @param userId The user id.
     * @return LocalDateTime The last login time.
     */
    public final LocalDateTime get(final String userId) {
        return cache.get(userId);
    }
}