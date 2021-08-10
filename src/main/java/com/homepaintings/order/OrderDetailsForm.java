package com.homepaintings.order;

import com.homepaintings.painting.PaintingType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class OrderDetailsForm {

    private String name;

    private PaintingType type;

    private Integer price;

    private Integer quantity;

}
