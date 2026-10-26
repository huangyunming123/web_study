package com.demo.biz.controller;

import com.demo.biz.common.R;
import com.demo.biz.entity.Region;
import com.demo.biz.service.RegionService;
import com.demo.biz.vo.RegionTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 行政地区字典控制器
 */
@Tag(name = "地区管理", description = "行政地区字典树及增删改查")
@RestController
@RequestMapping("/api/region")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @Operation(summary = "获取全量地区树 (带 Redis 缓存)")
    @GetMapping("/tree")
    public R<List<RegionTreeVO>> tree() {
        List<RegionTreeVO> tree = regionService.tree();
        return R.ok(tree);
    }

    @Operation(summary = "新增地区")
    @PostMapping
    public R<Void> add(@RequestBody Region region) {
        regionService.add(region);
        return R.ok();
    }

    @Operation(summary = "修改地区")
    @PutMapping("/{id}")
    public R<Void> update(
            @Parameter(description = "地区ID", required = true) @PathVariable Long id,
            @RequestBody Region region) {
        regionService.update(id, region);
        return R.ok();
    }

    @Operation(summary = "删除地区 (存在下级则拒绝)")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "地区ID", required = true) @PathVariable Long id) {
        regionService.delete(id);
        return R.ok();
    }
}
