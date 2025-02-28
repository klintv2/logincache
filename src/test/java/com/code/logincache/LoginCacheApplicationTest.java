package com.code.logincache;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LoginCacheApplicationTest {

    @Autowired
    private LoginService loginService;

    @Test
    public void loginTest() {

        // New entry will not be added to the cache as 1234 is not in the database.
        Assert.assertFalse(loginService.hasUserLoggedInWithin24("1234"));

        // New entry will be added to the cache.
        loginService.hasUserLoggedInWithin24("1235");
        Assert.assertEquals(1, loginService.getCacheSize());
        // Check that 1235 was added to the cache.
        Assert.assertTrue(loginService.isUserInCache("1235"));

        // New entry will be added to the cache.
        loginService.hasUserLoggedInWithin24("1236");
        Assert.assertEquals(2, loginService.getCacheSize());

        // Add more entries to fill up the cache to its max
        loginService.hasUserLoggedInWithin24("1237");
        loginService.hasUserLoggedInWithin24("1238");
        loginService.hasUserLoggedInWithin24("1239");
        loginService.hasUserLoggedInWithin24("1240");
        loginService.hasUserLoggedInWithin24("1241");
        loginService.hasUserLoggedInWithin24("1242");
        loginService.hasUserLoggedInWithin24("1243");
        loginService.hasUserLoggedInWithin24("1244");
        Assert.assertEquals(10, loginService.getCacheSize());

        // Add one more entry to go beyond the max cache size.
        // Entry 1235 should now be removed from the cache and 1245 added
        loginService.hasUserLoggedInWithin24("1245");
        Assert.assertFalse(loginService.isUserInCache("1235"));
        Assert.assertEquals(10, loginService.getCacheSize());

        // Add one more entry to go beyond the max cache size.
        // Entry 1236 should now be removed from the cache and 1246 added
        loginService.hasUserLoggedInWithin24("1246");
        Assert.assertFalse(loginService.isUserInCache("1236"));
        Assert.assertEquals(10, loginService.getCacheSize());

        // Check userJustLoggedIn will add new entry
        loginService.userJustLoggedIn("1247");
        Assert.assertTrue(loginService.isUserInCache("1247"));
        Assert.assertEquals(10, loginService.getCacheSize());

        // Check that userJustLoggedIn is not adding again
        loginService.userJustLoggedIn("1247");
        Assert.assertEquals(10, loginService.getCacheSize());

    }

}
