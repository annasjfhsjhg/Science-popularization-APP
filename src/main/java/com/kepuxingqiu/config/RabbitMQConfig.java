package com.kepuxingqiu.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CANAL_USER_QUEUE  = "canal.user.queue";
    public static final String CANAL_EXCHANGE     = "canal.exchange";
    public static final String CANAL_USER_ROUTING = "canal.user";

    /**
     * 队列声明必须与 Canal Server 保持一致（Canal 启动时不带 DLX 参数声明此队列）。
     * 若两边参数不同 RabbitMQ 会抛 PRECONDITION_FAILED，所以不加任何额外参数。
     */
    @Bean
    public Queue canalUserQueue() {
        return QueueBuilder.durable(CANAL_USER_QUEUE).build();
    }

    @Bean
    public DirectExchange canalExchange() {
        return new DirectExchange(CANAL_EXCHANGE, true, false);
    }

    @Bean
    public Binding canalUserBinding(Queue canalUserQueue, DirectExchange canalExchange) {
        return BindingBuilder.bind(canalUserQueue).to(canalExchange).with(CANAL_USER_ROUTING);
    }
}
