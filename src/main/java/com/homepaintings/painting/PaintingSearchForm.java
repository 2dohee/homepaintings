package com.homepaintings.painting;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PaintingSearchForm {

    private String type = "전체";

    private String keywords = "";

    private String sorting = "이름";

    private boolean asc;

}
