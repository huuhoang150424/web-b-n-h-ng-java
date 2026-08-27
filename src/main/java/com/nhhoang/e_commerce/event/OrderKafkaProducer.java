package com.nhhoang.e_commerce.event;

import com.nhhoang.e_commerce.config.KafkaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderKafkaProducer {

    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderPlacedEvent(OrderPlacedEvent event) {
        try {
            if (kafkaTemplate != null) {
                kafkaTemplate.send(KafkaConfig.ORDER_EVENTS_TOPIC, event.getOrderId(), event);
                System.out.println("🚀 [Kafka Producer] Sent OrderPlacedEvent to topic: " + event.getOrderCode());
            } else {
                System.out.println("⚠️ [Kafka Producer] KafkaTemplate not active, event logged: " + event.getOrderCode());
            }
        } catch (Exception e) {
            System.err.println("⚠️ [Kafka Producer Warning] Failed to publish Kafka event: " + e.getMessage());
        }
    }

    public void sendProductSyncEvent(ProductSyncEvent event) {
        try {
            if (kafkaTemplate != null) {
                kafkaTemplate.send(KafkaConfig.PRODUCT_SYNC_TOPIC, event.getProductId(), event);
                System.out.println("🚀 [Kafka Producer] Sent ProductSyncEvent to topic: " + event.getProductName());
            } else {
                System.out.println("⚠️ [Kafka Producer] KafkaTemplate not active, event logged: " + event.getProductName());
            }
        } catch (Exception e) {
            System.err.println("⚠️ [Kafka Producer Warning] Failed to publish Kafka product sync event: " + e.getMessage());
        }
    }
}
