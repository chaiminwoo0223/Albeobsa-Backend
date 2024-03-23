package skhu.jijijig.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlackListService {
    private final ConcurrentHashMap<String, Long> tokenBlackList = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public TokenBlackListService() {
        initializeTokenCleanupTask();
    }

    private void initializeTokenCleanupTask() {
        scheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.DAYS);
    }

    private void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        tokenBlackList.entrySet().removeIf(entry -> entry.getValue() < currentTime);
    }

    public void addToBlackList(String token) {
        long expiryTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7); // 7일 후 만료
        tokenBlackList.put(token, expiryTime);
    }

    public boolean isBlackListed(String token) {
        Long expiryTime = tokenBlackList.get(token);
        return expiryTime != null && expiryTime >= System.currentTimeMillis();
    }
}