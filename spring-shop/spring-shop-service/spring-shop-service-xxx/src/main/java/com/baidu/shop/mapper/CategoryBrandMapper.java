package com.baidu.shop.mapper;

import com.baidu.shop.entity.CategoryBrandEntity;

import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.entity.SpecParamEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CategoryBrandMapper extends Mapper<CategoryBrandEntity>, InsertListMapper<CategoryBrandEntity> {

    @Select(value = "SELECT * FROM tb_category_brand where category_id=#{id}")
    List<CategoryBrandEntity> getCategoryByBrand(Integer id);

    @Select(value = "SELECT * FROM tb_spec_group where cid=#{id}")
    List<SpecGroupEntity> getCategoryByGroup(Integer id);


}
