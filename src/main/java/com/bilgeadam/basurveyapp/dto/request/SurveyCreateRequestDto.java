package com.bilgeadam.basurveyapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SurveyCreateRequestDto {
    @NotBlank
    @NotNull
    private String surveyTitle;
    @NotBlank
    @NotNull
    private String courseTopic;
}