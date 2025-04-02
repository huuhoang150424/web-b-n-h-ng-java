package com.nhhoang.e_commerce.dto.requests;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestVnPay {
    private List<CartRequestVnPay> carts;
    private String shippingAddress;
    private String receiverName;
    private String receiverPhone;
}