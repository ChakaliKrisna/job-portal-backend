package com.jobportal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String techStack;
    private String githubLink;

    @ManyToOne
    @JoinColumn(name = "profile_id")
    @com.fasterxml.jackson.annotation.JsonBackReference
    private StudentProfile profile;
}