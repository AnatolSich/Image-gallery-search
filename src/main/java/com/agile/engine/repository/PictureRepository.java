package com.agile.engine.repository;

import com.agile.engine.model.Picture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PictureRepository extends JpaRepository<Picture, String> {

    @Query("FROM Picture AS p " +
            "WHERE p.id_old like '%\" +  ?1 + \"%' " +
            "OR p.author like '%\" +  ?1 + \"%' " +
            "OR p.camera like '%\" +  ?1 + \"%' " +
            "OR p.cropped_picture like '%\" +  ?1 + \"%' " +
            "OR p.full_picture like '%\" +  ?1 + \"%' " +
            "ORDER BY p.id DESC")
    List<Picture> findAll(String searchTerm);
}