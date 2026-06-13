package com.jobportal.entity;

//package com.jobportal.dto;

import com.jobportal.entity.Company;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompanyResponseDTO {

    private String publicId;
    private String name;
    private String location;
    private String website;
    private String industry;
    private String companySize;
    private Integer foundedYear;
    private String logoUrl;
    private String description;

    public CompanyResponseDTO(Company company) {
        this.publicId = company.getPublicId();
        this.name = company.getName();
        this.location = company.getLocation();
        this.website = company.getWebsite();
        this.industry = company.getIndustry();
        this.companySize = company.getCompanySize();
        this.foundedYear = company.getFoundedYear();
        this.logoUrl = company.getLogoUrl();
        this.description = company.getDescription();
    }
}