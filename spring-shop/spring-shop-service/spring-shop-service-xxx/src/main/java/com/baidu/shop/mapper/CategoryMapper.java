package com.baidu.shop.mapper;

import com.baidu.shop.entity.CategoryEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CategoryMapper extends Mapper<CategoryEntity>, SelectByIdListMapper<CategoryEntity, Integer> {

    @Select(value = "select c.id,c.name from tb_category c where c.id in (select cb.category_id from tb_category_brand cb where cb.brand_id=#{brandId})")
    List<CategoryEntity> getBybrandId(Integer brandId);

//    @Select(value = "select cb.brand_id  from tb_category c,tb_category_brand cb where c.id = cb.category_id and c.id =#{id}")
//    List<CategoryEntity> getCategoryBybrandId(Integer id);
}
