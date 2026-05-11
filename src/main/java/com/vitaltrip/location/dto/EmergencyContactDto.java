package com.vitaltrip.location.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmergencyContactDto {
    private final String fire;
    private final String police;
    private final String medical;
    private final String general;
}
