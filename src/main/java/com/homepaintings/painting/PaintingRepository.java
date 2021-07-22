package com.homepaintings.painting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface PaintingRepository extends JpaRepository<Painting, Long>, PaintingRepositoryExtension {

    boolean existsByName(String name);

}
