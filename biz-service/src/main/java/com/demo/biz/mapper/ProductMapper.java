package com.demo.biz.mapper;

import com.demo.biz.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * CRM 商品数据访问层 (原生 MyBatis Mapper)
 */
@Mapper
public interface ProductMapper {

    /**
     * 分页多条件查询商品列表
     */
    List<Product> selectPageList(@Param("offset") long offset,
                                 @Param("size") long size,
                                 @Param("keyword") String keyword,
                                 @Param("category") String category,
                                 @Param("status") Integer status);

    /**
     * 统计符合条件的商品总数
     */
    Long selectPageCount(@Param("keyword") String keyword,
                         @Param("category") String category,
                         @Param("status") Integer status);

    /**
     * 根据主键 ID 查询
     */
    Product selectById(@Param("id") Long id);

    /**
     * 新增商品 (商品发布，默认待审核)
     */
    int insert(Product product);

    /**
     * 修改商品
     */
    int update(Product product);

    /**
     * 更新上下架状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 审核商品 (通过/驳回)
     */
    int audit(@Param("id") Long id, @Param("status") Integer status, @Param("rejectReason") String rejectReason);

    /**
     * 扣减商品库存 (数据库级防超卖兜底: stock >= count)
     */
    int deductStock(@Param("id") Long id, @Param("count") int count);

    /**
     * 物理删除商品
     */
    int deleteById(@Param("id") Long id);
}
