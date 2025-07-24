package com.example.pss.model;

public enum ApplicationStatusEnum {
    // Added PENDING as it was missing and causing the IllegalArgumentException
    PENDING,
    SUBMITTED,
    UNDER_REVIEW,
    SELECTED,
    REJECTED
}