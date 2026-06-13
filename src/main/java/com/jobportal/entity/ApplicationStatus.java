package com.jobportal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;


public enum ApplicationStatus {
    APPLIED,
    REVIEWED,
    SHORTLISTED,
    INTERVIEW,
    REJECTED,
    HIRED
}