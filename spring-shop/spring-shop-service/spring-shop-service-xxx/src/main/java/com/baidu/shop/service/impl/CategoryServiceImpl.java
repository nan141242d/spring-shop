package com.baidu.shop.service.impl;

import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.mapper.CategoryMapper;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.service.CategoryService;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName CategoryServiceImpl
 * @Description: TODO
 * @Author huangyanan
 * @Date 2020/8/27
 * @Version V1.0
 **/
@RestController
public class CategoryServiceImpl extends BaseApiService implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;


    @Override
    public Result<List<CategoryEntity>> getCateByIdList(String cidsStr) {
        List<Integer> cidList = Arrays.asList(cidsStr.split(",")).stream().map(cidStr -> Integer.parseInt(cidStr))
                .collect(Collectors.toList());
        List<CategoryEntity> list = categoryMapper.selectByIdList(cidList);

        return this.setResultSuccess(list);
    }

    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {

        CategoryEntity categoryEntity = new CategoryEntity();

        categoryEntity.setParentId(pid);

        List<CategoryEntity> list = categoryMapper.select(categoryEntity);

        return this.setResultSuccess(list);
    }

    @Transactional
    @Override
    public Result<JsonObject> delCary(Integer id) {
        //通过当前id查询分类信息
        //判断是否有数据(安全)
        //判断当前节点是否是父级节点(安全)
        //判断当前节点的父节点下 除了当前节点是否还有别的节点(业务)
        //没有:将当前节点的父节点isParent的值修改为0
        //通过id删除数据

        CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(id);
        if (categoryEntity == null) {
            return this.setResultError("当前id不存在");
        }

        if (categoryEntity.getIsParent() == 1) {
            return this.setResultError("当前节点为父节点,不能删除");
        }
        List<CategoryBrandEntity> brandList = categoryBrandMapper.getCategoryByBrand(id);
        if (brandList.size() > 0) return this.setResultError("该分类信息被品牌绑定不能被删除!!");

        List<SpecGroupEntity> groupList = categoryBrandMapper.getCategoryByGroup(id);
        if (groupList.size() > 0) return this.setResultError("该分类信息绑定规格不能被删除!!");


        Example example = new Example(CategoryEntity.class);
        example.createCriteria().andEqualTo("parentId", categoryEntity.getParentId());
        //List<CategoryEntity> list = categoryMapper.selectByExample(example);
        Integer count = categoryMapper.selectCountByExample(example);

        if (count == 1) {
            CategoryEntity categoryEntity1 = new CategoryEntity();
            categoryEntity1.setId(categoryEntity.getParentId());
            categoryEntity1.setIsParent(0);
            categoryMapper.updateByPrimaryKeySelective(categoryEntity1);

        }

        categoryMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();

    }

    @Transactional
    @Override
    public Result<JsonObject> saveCategory(CategoryEntity categoryEntity) {
        //通过页面传递过来的parentid查询parentid对应的数据是否为父节点isParent==1
        //如果parentid对应的isParent != 1
        //需要修改为1
        //通过新增节点的父id将父节点的parent状态改为1
        CategoryEntity parentEntity = new CategoryEntity();
        parentEntity.setId(categoryEntity.getParentId());
        parentEntity.setIsParent(1);
        categoryMapper.updateByPrimaryKeySelective(parentEntity);
        categoryMapper.insertSelective(categoryEntity);

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JsonObject> editCategory(CategoryEntity categoryEntity) {
        categoryMapper.updateByPrimaryKeySelective(categoryEntity);
        return this.setResultSuccess();
    }

    @Override
    public Result<List<CategoryEntity>> getBybrand(Integer brandId) {
        List<CategoryEntity> list = categoryMapper.getBybrandId(brandId);
        return this.setResultSuccess(list);
    }
}
