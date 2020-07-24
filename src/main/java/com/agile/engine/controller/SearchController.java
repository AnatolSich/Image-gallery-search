package com.agile.engine.controller;

import com.agile.engine.repository.PictureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {
    private final PictureRepository pictureRepository;


    @Autowired
    public SearchController(PictureRepository pictureRepository) {
        this.pictureRepository = pictureRepository;
    }

    @GetMapping (value = "/search/{searchTerm}")
    String searchPicture(@PathVariable ("searchTerm") String searchTerm, Model model) {
        model.addAttribute("pictures", pictureRepository.findAll(searchTerm));
        return "pictures";
    }
}
