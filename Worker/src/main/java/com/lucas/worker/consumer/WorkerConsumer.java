package com.lucas.worker.consumer;

import com.lucas.common.redis.RedisUtils;
import com.lucas.configservice.dto.DispatchRequest;
import com.lucas.configservice.dto.DispatchResponse;
import com.lucas.worker.entity.Accounts;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.util.Date;

@Log4j2
@Component
public class WorkerConsumer {

    @Autowired
    private EntityManager entityManager;

    @Transactional
    @SendTo
    @KafkaListener(topics = "${com.lucas.kafka.synchronous.requestTopic}", groupId = "${spring.kafka.consumer.group-id}")
    public DispatchResponse workerConsumerProcess(DispatchRequest key) {
        log.info("WorkerConsumer process key: {}", key.content());

        Accounts entity = RedisUtils.getObject(key.content(), Accounts.class);

        if (entity == null) {
            log.error("WorkerConsumer process key: {} is null", key);
            return new DispatchResponse("WorkerConsumer process key: " + key + " is null");
        }

        log.info("Entity: {}", entity);

        RedisUtils.delete(key.content());

        try {
            String sql = "UPDATE `accounts` SET status = ?, updated = ? WHERE id = ?";

            entityManager.createNativeQuery(sql).setParameter(1, 1).setParameter(2, new Date()).setParameter(3, entity.getId()).executeUpdate();

            Accounts accountUpdate = entityManager.find(Accounts.class, entity.getId());

            RedisUtils.setObject(key.content(), accountUpdate);

            log.info("Updated record count: {}", accountUpdate);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return new DispatchResponse("Error WorkerConsumer process key: " + key);
        }
        return new DispatchResponse("WorkerConsumer process key: " + key);
    }
}
