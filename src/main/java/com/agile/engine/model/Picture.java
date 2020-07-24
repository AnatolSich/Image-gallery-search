package com.agile.engine.model;

import lombok.Builder;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Entity
@Table(name = "picture")
public class Picture {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String id_old;
        private String author;
        private String camera;
        private String cropped_picture;
        private String full_picture;
    }

