package com.nhhoang.e_commerce.event;

import com.nhhoang.e_commerce.config.KafkaConfig;
import com.nhhoang.e_commerce.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderKafkaConsumer {

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = KafkaConfig.ORDER_EVENTS_TOPIC, groupId = "ecommerce-group", autoStartup = "false")
    public void consumeOrderPlacedEvent(OrderPlacedEvent event) {
        System.out.println("📩 [Kafka Consumer] Received OrderPlacedEvent for order: " + event.getOrderCode());

        // Send order confirmation email asynchronously via Event Consumer
        if (event.getUserEmail() != null) {
            String subject = "Xác nhận đơn hàng #" + event.getOrderCode();
            String content = String.format("Chào %s,\n\nĐơn hàng #%s của bạn trị giá %,.0f đ đã đặt thành công và đang được xử lý!\n\nĐịa chỉ giao hàng: %s",
                    event.getUserName(), event.getOrderCode(), event.getTotalAmount(), event.getShippingAddress());
            emailService.sendEmailAsync(event.getUserEmail(), subject, content);
        }
    }
}
