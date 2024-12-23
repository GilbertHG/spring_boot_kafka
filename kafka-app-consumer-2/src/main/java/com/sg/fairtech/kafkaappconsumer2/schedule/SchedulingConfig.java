package com.sg.fairtech.kafkaappconsumer2.schedule;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Profile("scheduling-enabled")
public class SchedulingConfig {
}
