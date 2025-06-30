package com.redizego.redi_ze_go.advices;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@Builder
public class ApiError {

    private String message;
    private HttpStatus status;
    private List<String> errors;

}
