package com.nhhoang.e_commerce.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPlacedEvent implements Serializable {
    private String orderId;
    private String orderCode;
    private String userId;
    private String userEmail;
    private String userName;
    private Float totalAmount;
    private String shippingAddress;
    private String receiverPhone;
    private LocalDateTime timestamp;
}
