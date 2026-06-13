package com.jobportal.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String publicId;

    @Column(nullable = false)
    private String name;

    private String location;

    private String website;

    private String industry;

    private String companySize;

    private Integer foundedYear;

    private String logoUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "company")
    @JsonIgnore
    private List<User> recruiters;

    @OneToMany(mappedBy = "company")
    @JsonIgnore
    private List<Job> jobs;

    @PrePersist
    public void onCreate() {

        if (this.publicId == null) {
            this.publicId = UUID.randomUUID().toString();
        }

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}