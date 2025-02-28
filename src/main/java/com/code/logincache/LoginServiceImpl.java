package com.code.logincache;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * LoginService implementation.
 */
@Component
public class LoginServiceImpl implements LoginService {

    @Autowired
    private FakeDBAccess fakeDBAccess;

    @Autowired
    private LoginCache loginCache;

    /**
     * Checks whether the user has logged in within the last 24 hours.
     * does NOT imply a login
     *
     * @param userId The user id
     * @return True if the user HAS logged in within 24 hours
     * False otherwise
     */
    @Override
    public final boolean hasUserLoggedInWithin24(final String userId) {
        if (StringUtils.isEmpty(userId)) {
            return false;
        }
        synchronized (loginCache) {
            LocalDateTime lastLoginForUser = loginCache.get(userId);
            if (lastLoginForUser == null) {
                // User is not in the cache, lets check the database.
                lastLoginForUser = fakeDBAccess.getLastLoginForUser(userId);
                if (lastLoginForUser != null) {
                    loginCache.addToCache(userId, lastLoginForUser);
                }
            }
            return lastLoginForUser != null ? lastLoginForUser.isAfter(LocalDateTime.now().minusHours(24)) : false;
        }
    }

    /**
     * Sets the last login time for the user to now.
     *
     * @param userId The user id.
     */
    @Override
    public final void userJustLoggedIn(final String userId) {
        if (!StringUtils.isEmpty(userId)) {
            synchronized (loginCache) {
                final LocalDateTime now = LocalDateTime.now();
                // I assume that every time a user is logging in we need to update the DB also.
                fakeDBAccess.setLastLoginForUser(userId, now);
                loginCache.addToCache(userId, now);
            }
        }
    }

    /**
     * Return the current size of the cache.
     * @return int current size of the cache.
     */
    @Override
    public final int getCacheSize() {
        return loginCache.getCacheSize();
    }

    /**
     * Validates if the specified user id is present in the cache.
     * @param userId The user id.
     * @return boolean true if the specified user is in the cache.
     */
    @Override
    public final boolean isUserInCache(final String userId) {
        if (StringUtils.isEmpty(userId)) {
            return false;
        }
        return loginCache.isUserInCache(userId);
    }
}
