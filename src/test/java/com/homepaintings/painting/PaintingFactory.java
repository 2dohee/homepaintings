package com.homepaintings.painting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PaintingFactory {

    @Autowired PaintingRepository paintingRepository;

    public Painting saveNewPainting(String name, PaintingType type, int price, int stock) {
        Painting painting = Painting.builder()
                .name(name).type(type).price(price).stock(stock)
                .image("").description(name).createdDateTime(LocalDateTime.now()).updatedDateTime(LocalDateTime.now())
                .build();
        Painting savedPainting = paintingRepository.save(painting);
        paintingRepository.flush();
        return savedPainting;
    }

}
