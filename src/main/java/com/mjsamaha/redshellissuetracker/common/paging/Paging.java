package com.mjsamaha.redshellissuetracker.common.paging;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class Paging {
    public static final int MAX_SIZE = 100;
    private static final int DEFAULT_SIZE = 20;

    private Paging() { }

    public static Pageable pageable(int page, int size, String sortField, Sort.Direction direction,
                                    Set<String> allowedSortFields) {
        if (page < 0) throw new IllegalArgumentException("Page index must be zero or greater");
        int safeSize = size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        Sort sort = Sort.unsorted();
        if (sortField != null && !sortField.isBlank()) {
            if (!allowedSortFields.contains(sortField)) {
                throw new IllegalArgumentException("Unsupported sort field: " + sortField);
            }
            sort = Sort.by(direction == null ? Sort.Direction.DESC : direction, sortField);
        }
        return PageRequest.of(page, safeSize, sort);
    }
}
