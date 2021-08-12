package com.homepaintings.order;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter @Setter
public class DeliveryStatusForm {

    private ArrayList<Long> orderIdList = new ArrayList<>();

    private ArrayList<DeliveryStatus> deliveryStatusList = new ArrayList<>();

}
