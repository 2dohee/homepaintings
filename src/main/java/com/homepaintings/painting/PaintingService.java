package com.homepaintings.painting;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaintingService {

    private final PaintingRepository paintingRepository;
    private final ModelMapper modelMapper;

    public void createPainting(PaintingForm paintingForm) {
        Painting newPainting = modelMapper.map(paintingForm, Painting.class);
        newPainting.setCreatedDateTime(LocalDateTime.now());
        newPainting.setUpdatedDateTime(LocalDateTime.now());
        paintingRepository.save(newPainting);
    }

}
