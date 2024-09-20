package com.bradwick.tibia_genius.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Set;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ExceptionResponse {

    private Integer errorCode;
    private String error;
    private String errorDescription;
    private Set<String> validationErrors;
    private Map<String, String> errors;
}
