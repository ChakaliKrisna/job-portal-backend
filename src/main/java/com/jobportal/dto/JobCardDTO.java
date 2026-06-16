package com.jobportal.dto;

//package com.jobportal.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobCardDTO {

    private String publicId;
    private String title;
    private String location;
    private Double salary;

    private String jobType;
    private String workMode;

    private String companyName;
    private String companyLogo;
}