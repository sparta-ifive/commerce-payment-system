package com.spartaifive.commercepayment.common.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class TimeService {
    private static Clock clock;

    @Autowired
    private Clock injectedClock;

    @PostConstruct
    public void init() {
        TimeService.clock = this.injectedClock;
    }

    public static LocalDateTime getCurrentTime() {
        if (TimeService.clock == null) {
            return LocalDateTime.now();
        }
        return LocalDateTime.now(clock);
    }
}
