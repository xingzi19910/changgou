package com.changgou.goods.dao;

import com.changgou.goods.pojo.Spec;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SpecMapper extends Mapper<Spec> {

    //根据商品分类名称查询规格列表
    @Select("SELECT s.`name`,s.`options` \n" +
            "FROM `tb_category` c ,`tb_template` t ,`tb_spec` s WHERE s.`template_id`=t.`id` AND t.`id`=c.`template_id`AND c.`name`=#{name} GROUP BY s.`id` ")
    public List<Map<String,Object>> findSpecByCategoryName(@Param("name") String categoryName);
}
