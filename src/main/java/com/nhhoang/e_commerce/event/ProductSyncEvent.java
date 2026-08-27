package com.nhhoang.e_commerce.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSyncEvent implements Serializable {
    private String productId;
    private String productName;
    private String slug;
    private String description;
    private Float price;
    private Integer stock;
    private String categoryName;
    private String thumbImage;
    private ActionType action;

    public enum ActionType {
        CREATE,
        UPDATE,
        DELETE
    }
}
