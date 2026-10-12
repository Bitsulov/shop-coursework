package com.example.shop.dtos.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@Schema(description = "Страница списка")
public class PageResponse<T> {

    @Schema(description = "Элементы страницы")
    private List<T> content;

    @Schema(description = "Номер страницы, начиная с нуля", example = "0")
    private int page;

    @Schema(description = "Размер страницы", example = "24")
    private int size;

    @Schema(description = "Всего элементов", example = "15")
    private long totalElements;

    @Schema(description = "Всего страниц", example = "1")
    private int totalPages;

    public static <T> PageResponse<T> of(Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setContent(page.getContent());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        return response;
    }
}
