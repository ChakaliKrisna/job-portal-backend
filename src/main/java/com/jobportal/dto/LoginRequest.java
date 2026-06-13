package com.jobportal.dto;

//package com.jobportal.dto;

import lombok.Data;
//mport lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}