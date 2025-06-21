package com.ach.eisenhower.services;

import com.ach.eisenhower.repositories.EisenhowerUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

@Component
public class DatabaseCleanupService {
    private final EisenhowerUserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(DatabaseCleanupService.class);

    public DatabaseCleanupService(EisenhowerUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void CleanupInactiveUsers() {
        var twoYearsAgo = LocalDate.now().minusYears(2);
        var inactiveUsers = userRepository.findByLastLoginDateBefore(
            Date.from(twoYearsAgo.atStartOfDay().toInstant(ZoneOffset.UTC))
        );
        logger.atInfo().log(String.format("Deleted %d inactive user(s)", inactiveUsers.size()));
        userRepository.deleteAll(inactiveUsers);
    }
}
