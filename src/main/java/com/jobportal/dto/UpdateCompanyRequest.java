package com.jobportal.dto;

//package com.jobportal.dto;

import lombok.Data;

@Data
public class UpdateCompanyRequest {

    private String name;

    private String location;

    private String website;

    private String description;

    private String industry;

    private String companySize;

    private Integer foundedYear;

    private String logoUrl;
}