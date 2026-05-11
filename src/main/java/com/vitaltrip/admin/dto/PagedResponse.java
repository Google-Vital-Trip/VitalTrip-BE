package com.vitaltrip.admin.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class PagedResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;
    private final boolean hasContent;
    private final boolean hasNext;
    private final boolean hasPrevious;

    public PagedResponse(List<T> content, int page, int size, long totalElements,
                         int totalPages, boolean first, boolean last,
                         boolean hasContent, boolean hasNext, boolean hasPrevious) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
        this.hasContent = hasContent;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    public static <E, R> PagedResponse<R> from(Page<E> page, Function<E, R> mapper) {
        List<R> content = page.getContent().stream().map(mapper).collect(Collectors.toList());
        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasContent(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
