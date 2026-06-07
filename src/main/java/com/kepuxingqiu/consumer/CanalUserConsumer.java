package com.kepuxingqiu.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kepuxingqiu.common.canal.CanalMessage;
import com.kepuxingqiu.common.utils.RedisUtils;
import com.kepuxingqiu.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CanalUserConsumer {

    private static final String CACHE_KEY_PREFIX = "user:level:";
    private static final long   CACHE_TTL_SECONDS = 3600L;

    private final RedisUtils redisUtils;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.CANAL_USER_QUEUE)
    public void handleUserChange(String payload, Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            CanalMessage canal = objectMapper.readValue(payload, CanalMessage.class);

            // 忽略 DDL 或非 user 表的消息
            if (canal.isDdl() || !"user".equals(canal.getTable())) {
                channel.basicAck(deliveryTag, false);
                return;
            }

            String type = canal.getType();
            List<Map<String, Object>> dataList = canal.getData();
            if (dataList == null || dataList.isEmpty()) {
                channel.basicAck(deliveryTag, false);
                return;
            }

            switch (type) {
                case "INSERT", "UPDATE" -> dataList.forEach(this::refreshLevelCache);
                case "DELETE"           -> dataList.forEach(this::evictLevelCache);
                default                 -> log.debug("Ignored canal event type: {}", type);
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("Canal消息处理失败，转入死信队列. payload={}", payload, e);
            // requeue=false → 超过重试后进死信队列
            channel.basicNack(deliveryTag, false, false);
        }
    }

    /**
     * INSERT / UPDATE：用 Canal data 中的最新值刷新缓存。
     * Canal 的 data 字段始终包含该行所有字段的最新值，无需额外查库。
     */
    private void refreshLevelCache(Map<String, Object> row) {
        Object id      = row.get("id");
        Object level   = row.get("level");
        Object exp     = row.get("experience");

        if (id == null) return;

        String cacheKey = CACHE_KEY_PREFIX + id;

        if (level != null) redisUtils.hset(cacheKey, "level", String.valueOf(level));
        if (exp   != null) redisUtils.hset(cacheKey, "exp",   String.valueOf(exp));

        if (level != null || exp != null) {
            redisUtils.expire(cacheKey, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            log.info("Canal缓存刷新: {} level={} exp={}", cacheKey, level, exp);
        }
    }

    /** DELETE：直接删除缓存，下次读时走数据库 */
    private void evictLevelCache(Map<String, Object> row) {
        Object id = row.get("id");
        if (id != null) {
            redisUtils.delete(CACHE_KEY_PREFIX + id);
            log.info("Canal缓存清除: {}", CACHE_KEY_PREFIX + id);
        }
    }
}
