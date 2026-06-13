package com.jobportal.controller;

import com.jobportal.dto.UpdateCompanyRequest;
import com.jobportal.entity.Company;
import com.jobportal.entity.CompanyResponseDTO;
import com.jobportal.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/job-portal/company")
@RequiredArgsConstructor
public class CompanyController {


    private final CompanyService companyService;

    @GetMapping("/my-company")
    @PreAuthorize("hasRole('RECRUITER')")
    public Company getMyCompany(
            Authentication authentication
    ) {
        System.out.println("CONTROLLER HIT");
        return companyService.getMyCompany(authentication);
    }



    @PutMapping("/update")
    @PreAuthorize("hasRole('RECRUITER')")
    public Company updateCompany(
            @RequestBody UpdateCompanyRequest request,
            Authentication authentication
    ) {

        return companyService.updateCompany(
                request,
                authentication
        );
    }
    @GetMapping
    public List<CompanyResponseDTO> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    @GetMapping("/{publicId}")
    public CompanyResponseDTO getCompanyByPublicId(
            @PathVariable String publicId
    ) {
        System.out.println("Company Controller Hit: " + publicId);

        return companyService.getCompanyByPublicId(publicId);
    }
}