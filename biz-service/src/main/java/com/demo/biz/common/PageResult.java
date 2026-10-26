package com.demo.biz.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 通用分页查询结果封装 (教学原生 MyBatis 统一分页结构)
 * 与前端 Element Plus 分页组件无缝对接
 *
 * @param <T> 数据泛型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一分页结果")
public class PageResult<T> implements Serializable {

    @Schema(description = "当前页数据列表")
    private List<T> records;

    @Schema(description = "总记录条数", example = "100")
    private Long total;

    @Schema(description = "当前页码", example = "1")
    private Long current;

    @Schema(description = "每页展示条数", example = "10")
    private Long size;

    public static <T> PageResult<T> of(List<T> records, Long total, Long current, Long size) {
        return PageResult.<T>builder()
                .records(records != null ? records : Collections.emptyList())
                .total(total != null ? total : 0L)
                .current(current)
                .size(size)
                .build();
    }
}
