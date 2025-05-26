package com.lucas.worker.consumer;

import com.lucas.worker.entity.Accounts;
import com.lucas.worker.utils.RedisUtils;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;

@Log4j2
@Component
public class WorkerConsumer {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RedisUtils redisUtils;

    @Transactional
    @KafkaListener(topics = "ACCOUNTS", groupId = "demoRedisKafka")
    public void workerConsumerProcess(String key) {
        log.info("WorkerConsumer process key: {}", key);

        Accounts entity = redisUtils.getObject(key, Accounts.class);
        log.info("Entity: {}", entity);
        redisUtils.delete(key);
        try {
            String sql = "UPDATE `accounts` SET status = ?, updated = ? WHERE id = ?";

            entityManager.createNativeQuery(sql)
                    .setParameter(1, 1)
                    .setParameter(2, new Date())
                    .setParameter(3, entity.getId())
                    .executeUpdate();

            Accounts accountUpdate = entityManager.find(Accounts.class, entity.getId());
            redisUtils.setObject(key, accountUpdate);
            log.info("Updated record count: {}", accountUpdate);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
