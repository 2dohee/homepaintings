package com.homepaintings.painting;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
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

    public void updatePainting(PaintingForm paintingForm, Painting painting) {
        modelMapper.map(paintingForm, painting);
        painting.setUpdatedDateTime(LocalDateTime.now());
    }

    public void deletePainting(Painting painting) {
        paintingRepository.delete(painting);
    }
}
