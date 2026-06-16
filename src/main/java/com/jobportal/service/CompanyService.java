package com.jobportal.service;

//package com.jobportal.service;

import com.jobportal.dto.UpdateCompanyRequest;
import com.jobportal.entity.Company;
import com.jobportal.entity.CompanyResponseDTO;
import com.jobportal.entity.User;
import com.jobportal.repository.CompanyRepository;
import com.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public Company getMyCompany(Authentication authentication) {

        String email = authentication.getName();

        User recruiter = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return recruiter.getCompany();
    }

    public Company updateCompany(
            UpdateCompanyRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        User recruiter = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Company company = recruiter.getCompany();

        if(company == null) {
            throw new RuntimeException("Company not found");
        }
        if (request.getLogoUrl() != null &&
                request.getLogoUrl().length() > 2000) {

            throw new RuntimeException("Logo URL is too long");
        }

        company.setName(request.getName());
        company.setLocation(request.getLocation());
        company.setWebsite(request.getWebsite());
        company.setDescription(request.getDescription());
        company.setIndustry(request.getIndustry());
        company.setCompanySize(request.getCompanySize());
        company.setFoundedYear(request.getFoundedYear());
        company.setLogoUrl(request.getLogoUrl());

        return companyRepository.save(company);
    }


    public CompanyResponseDTO getCompanyByPublicId(String publicId) {

        Company company = companyRepository.findByPublicId(publicId)
                .orElseThrow(() ->
                        new RuntimeException("Company not found with publicId: " + publicId));

        return new CompanyResponseDTO(company);
    }

    public List<CompanyResponseDTO> getAllCompanies() {

        return companyRepository.findAll()
                .stream()
                .map(CompanyResponseDTO::new)
                .toList();
    }
}