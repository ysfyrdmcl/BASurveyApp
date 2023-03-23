package com.bilgeadam.basurveyapp.dto.response;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SurveyStudentResponseDto {

    private String email;
    private String firstName;
    private String lastName;
}
