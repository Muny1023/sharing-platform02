package org.example.contentservice.entity.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AiSearchRequest(String keyword,
                              @Min(1) int page,
                              @Min(1) @Max(10) int size) {
}
