package ar.com.telecom.gid.doblefactor.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

public class SessionCache {

    private static Logger log = LoggerFactory.getLogger(SessionCache.class);

    private static final Cache<String, String> sessionCache = Caffeine.newBuilder()
            .expireAfterWrite(3, TimeUnit.MINUTES)
            .maximumSize(4000)
            .build();

    public static void storeSession(String sessionId, String jwtToken) {
        sessionCache.put(sessionId, jwtToken);
    }

    public static String getSession(String sessionId) {
        return sessionCache.getIfPresent(sessionId);
    }

    public static void removeSession(String sessionId) {
        sessionCache.invalidate(sessionId);
    }
}

