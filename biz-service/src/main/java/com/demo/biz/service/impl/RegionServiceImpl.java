package com.demo.biz.service.impl;

import com.demo.biz.common.BusinessException;
import com.demo.biz.entity.Region;
import com.demo.biz.mapper.RegionMapper;
import com.demo.biz.service.RegionService;
import com.demo.biz.vo.RegionTreeVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 地区字典服务实现类 (基于原生 MyBatis + Redis 缓存)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    private final RegionMapper regionMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String REGION_TREE_CACHE_KEY = "region:tree";

    @Override
    public List<RegionTreeVO> tree() {
        // 1. 尝试从 Redis 缓存中获取
        try {
            String cacheJson = stringRedisTemplate.opsForValue().get(REGION_TREE_CACHE_KEY);
            if (StringUtils.hasText(cacheJson)) {
                log.info("命中 Redis 地区树缓存: key={}", REGION_TREE_CACHE_KEY);
                return objectMapper.readValue(cacheJson, new TypeReference<List<RegionTreeVO>>() {});
            }
        } catch (Exception e) {
            log.error("读取 Redis 地区树缓存失败，降级查询数据库: ", e);
        }

        // 2. 缓存未命中，执行原生 MyBatis 查询全量 region
        List<Region> allRegions = regionMapper.selectAll();
        List<RegionTreeVO> treeResult = buildTree(allRegions);

        // 3. 写入 Redis 缓存，TTL 10 分钟
        try {
            stringRedisTemplate.opsForValue().set(
                    REGION_TREE_CACHE_KEY,
                    objectMapper.writeValueAsString(treeResult),
                    Duration.ofMinutes(10)
            );
            log.info("地区树成功写入 Redis 缓存 (TTL 10min)");
        } catch (Exception e) {
            log.error("写入 Redis 地区树缓存异常: ", e);
        }

        return treeResult;
    }

    @Override
    public void add(Region region) {
        if (!StringUtils.hasText(region.getName())) {
            throw new BusinessException(500, "地区名称不能为空");
        }
        if (region.getParentId() == null) {
            region.setParentId(0L);
        }
        if (region.getLevel() == null) {
            region.setLevel(region.getParentId() == 0L ? 1 : 2);
        }
        region.setId(null);
        regionMapper.insert(region);
        log.info("新增地区成功: id={}, name={}", region.getId(), region.getName());

        clearCache();
    }

    @Override
    public void update(Long id, Region region) {
        Region existing = regionMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(500, "目标地区不存在");
        }
        region.setId(id);
        regionMapper.update(region);
        log.info("修改地区成功: id={}", id);

        clearCache();
    }

    @Override
    public void delete(Long id) {
        Region existing = regionMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(500, "待删除地区不存在");
        }

        // 检查是否存在子节点，若存在则拒绝删除
        Long childCount = regionMapper.countByParentId(id);
        if (childCount != null && childCount > 0) {
            throw new BusinessException(500, "存在子地区，无法删除");
        }

        regionMapper.deleteById(id);
        log.info("物理删除地区成功: id={}", id);

        clearCache();
    }

    private void clearCache() {
        try {
            Boolean deleted = stringRedisTemplate.delete(REGION_TREE_CACHE_KEY);
            log.info("删除地区树 Redis 缓存结果: key={}, success={}", REGION_TREE_CACHE_KEY, deleted);
        } catch (Exception e) {
            log.error("删除地区树 Redis 缓存异常: ", e);
        }
    }

    private List<RegionTreeVO> buildTree(List<Region> regions) {
        Map<Long, RegionTreeVO> map = new LinkedHashMap<>();
        for (Region r : regions) {
            map.put(r.getId(), RegionTreeVO.builder()
                    .id(r.getId())
                    .parentId(r.getParentId())
                    .name(r.getName())
                    .level(r.getLevel())
                    .children(new ArrayList<>())
                    .build());
        }

        List<RegionTreeVO> roots = new ArrayList<>();
        for (RegionTreeVO node : map.values()) {
            if (node.getParentId() == null || node.getParentId() == 0L) {
                roots.add(node);
            } else {
                RegionTreeVO parent = map.get(node.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    roots.add(node);
                }
            }
        }
        return roots;
    }
}
