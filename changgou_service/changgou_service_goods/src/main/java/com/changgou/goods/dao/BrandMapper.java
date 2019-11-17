package com.changgou.goods.dao;

import com.changgou.goods.pojo.Brand;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {
    //根据分类名称查询品牌列表
    @Select("SELECT b.`name`, b.`image`\n" +
            "FROM tb_brand b, `tb_category` c,`tb_category_brand` cb\n" +
            "WHERE b.`id`= cb.`brand_id` AND c.`id` = cb.`category_id` AND c.`name`=#{categoryName};")
    public List<Brand> findBrandByCategoryName(@Param("categoryName") String categoryName);
}
