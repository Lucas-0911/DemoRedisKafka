package com.lucas.configservice.config;

import com.lucas.configservice.dto.DispatchRequest;
import com.lucas.configservice.dto.DispatchResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(SynchronousKafkaProperties.class)
public class KafkaConfiguration {

    private final SynchronousKafkaProperties synchronousKafkaProperties;

    KafkaConfiguration(SynchronousKafkaProperties synchronousKafkaProperties) {
        this.synchronousKafkaProperties = synchronousKafkaProperties;
    }

    @Bean
    KafkaMessageListenerContainer<String, DispatchResponse> kafkaMessageListenerContainer(ConsumerFactory<String, DispatchResponse> consumerFactory) {
        String replyTopic = synchronousKafkaProperties.getReplyTopic();
        ContainerProperties containerProperties = new ContainerProperties(replyTopic);
        return new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
    }

    @Bean
    ReplyingKafkaTemplate<String, DispatchRequest, DispatchResponse> replyingKafkaTemplate(ProducerFactory<String, DispatchRequest> producerFactory, KafkaMessageListenerContainer<String, DispatchResponse> kafkaMessageListenerContainer) {
        Duration replyTimeout = synchronousKafkaProperties.getReplyTimeout();
        var replyingKafkaTemplate = new ReplyingKafkaTemplate<>(producerFactory, kafkaMessageListenerContainer);
        replyingKafkaTemplate.setDefaultReplyTimeout(replyTimeout);
        return replyingKafkaTemplate;
    }

    @Bean
    KafkaTemplate<String, DispatchResponse> kafkaTemplate(ProducerFactory<String, DispatchResponse> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, DispatchRequest>> kafkaListenerContainerFactory(ConsumerFactory<String, DispatchRequest> consumerFactory, KafkaTemplate<String, DispatchResponse> kafkaTemplate) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, DispatchRequest>();
        factory.setConsumerFactory(consumerFactory);
        factory.setReplyTemplate(kafkaTemplate);
        return factory;
    }

}
