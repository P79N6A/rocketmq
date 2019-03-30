package com.alibaba.rocketmq.client;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;


public class SimpleConsumerProducerTest {
    private static final String TOPIC_TEST = "pan_test_topic";

    @Test
    public void producerConsumerTest() throws MQClientException, InterruptedException {
        System.setProperty("rocketmq.namesrv.domain", "jmenv.tbsite.alipay.net");

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();
        consumer.setConsumerGroup("pan_consumer_group");
        consumer.setNamesrvAddr("10.230.54.27:9876");
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.subscribe(TOPIC_TEST, null);
        
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setProducerGroup("pan_produc_group");
        producer.setNamesrvAddr("10.230.54.27:9876");

        final AtomicLong lastReceivedMills = new AtomicLong(System.currentTimeMillis());

        final AtomicLong consumeTimes = new AtomicLong(0);

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            public ConsumeConcurrentlyStatus consumeMessage(final List<MessageExt> msgs,
                                                            final ConsumeConcurrentlyContext context) {
                System.out.println("Received" + consumeTimes.incrementAndGet() + "messages !");

                lastReceivedMills.set(System.currentTimeMillis());

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();
        producer.start();

        for (int i = 0; i < 100; i++) {
            try {
                Message msg = new Message(TOPIC_TEST, ("Hello RocketMQ " + i).getBytes());
                SendResult sendResult = producer.send(msg);
                System.out.println(sendResult);
            } catch (Exception e) {
                TimeUnit.SECONDS.sleep(1);
            }
        }

        // wait no messages
        while ((System.currentTimeMillis() - lastReceivedMills.get()) < 5000) {
            TimeUnit.MILLISECONDS.sleep(200);
        }

        consumer.shutdown();
        producer.shutdown();
    }
}
