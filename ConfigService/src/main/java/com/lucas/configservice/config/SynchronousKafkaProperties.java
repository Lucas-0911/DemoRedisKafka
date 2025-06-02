package com.lucas.configservice.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;


@Data
@ConfigurationProperties(prefix = "com.lucas.kafka.synchronous")
public class SynchronousKafkaProperties {
    @NotBlank
    private String requestTopic;

    @NotBlank
    private String replyTopic;

    @NotNull
    private Duration replyTimeout;
}
