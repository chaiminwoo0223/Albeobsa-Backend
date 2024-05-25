package skhu.jijijig.token;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlackListService {
    private final Map<String, Long> tokenBlackList = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void init() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.HOURS);
    }

    @PreDestroy
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    public void addToBlackList(String token) {
        tokenBlackList.put(token, System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
    }

    public boolean isBlackListed(String token) {
        return tokenBlackList.getOrDefault(token, 0L) > System.currentTimeMillis();
    }

    private void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        tokenBlackList.entrySet().removeIf(entry -> entry.getValue() < currentTime);
    }
}