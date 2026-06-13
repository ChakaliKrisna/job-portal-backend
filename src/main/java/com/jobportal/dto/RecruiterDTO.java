package com.jobportal.dto;

import com.jobportal.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

//package com.jobportal.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterDTO {

    private String publicId;
    private String name;
    private String email;
    public RecruiterDTO(User user) {
        this.publicId = user.getPublicId();
        this.name = user.getName();
        this.email = user.getEmail();
    }
}