package org.example.contentservice.entity.response;

import java.util.List;

public record PageResponse<T>(long page, long size, long total, List<T> items) {
}
