package com.baidu.shop.service.impl;

import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.entity.SpuEntity;
import com.baidu.shop.mapper.BrandMapper;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.mapper.SpuMapper;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.PinyinUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName BrandServiceImpl
 * @Description: TODO
 * @Author huangyanan
 * @Date 2020/8/31
 * @Version V1.0
 **/
@RestController
public class BrandServiceImpl extends BaseApiService implements BrandService {
    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    @Resource
    private SpuMapper spuMapper;

    @Override
    public Result<List<BrandEntity>> getBeandbyIdList(String brandsStr) {
        List<Integer> bList = Arrays.asList(brandsStr.split(",")).stream().map(bStr -> Integer.parseInt(bStr))
                .collect(Collectors.toList());
        List<BrandEntity> list = brandMapper.selectByIdList(bList);

        return this.setResultSuccess(list);
    }

    @Override
    public Result<PageInfo<BrandEntity>> getBrandByCate(Integer cid) {
        List<BrandEntity> list = brandMapper.getBrandByCateId(cid);
        return this.setResultSuccess(list);
    }

    @Override
    public Result<PageInfo<BrandEntity>> getList(BrandDTO brandDTO) {

        PageHelper.startPage(brandDTO.getPage(), brandDTO.getRows());

        Example example = new Example(BrandEntity.class);

        if (StringUtil.isNotEmpty(brandDTO.getSort())) example.setOrderByClause(brandDTO.getOrderByClauser());

        if (StringUtil.isNotEmpty(brandDTO.getName()))
            example.createCriteria().andLike("name", "%" + brandDTO.getName() + "%");

        List<BrandEntity> list = brandMapper.selectByExample(example);

        //数据封装
        PageInfo<BrandEntity> pageInfo = new PageInfo<>(list);

        return this.setResultSuccess(pageInfo);

    }

    @Transactional
    @Override
    public Result<JsonObject> saveBrand(BrandDTO brandDTO) {

        //获取到品牌名称/获取到品牌名称第一个字符/将第一个字符转换为pinyin/获取拼音的首字母/统一转为大写
        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);
        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().charAt(0)),
                PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));

//        try {
//            Thread.sleep(2000);
//            brandMapper.insertSelective(brandEntity);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        brandMapper.insertSelective(brandEntity);

        this.insertCategoryBrand(brandDTO, brandEntity);

        return this.setResultSuccess();

    }

    @Override
    public Result<JsonObject> editBrand(BrandDTO brandDTO) {
        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);
        //获取到品牌名称/获取到品牌名称第一个字符/将第一个字符转换为pinyin/获取拼音的首字母/统一转为大写
        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().charAt(0)),
                PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));
        brandMapper.updateByPrimaryKeySelective(brandEntity);

        //删除中间表得数据
        this.deleteCategoryBrand(brandEntity.getId());

        this.insertCategoryBrand(brandDTO, brandEntity);

        return this.setResultSuccess();
    }


    private void insertCategoryBrand(BrandDTO brandDTO, BrandEntity brandEntity) {
        //分割 得到数组, 批量新增
        if (brandDTO.getCategory().contains(",")) {

            List<CategoryBrandEntity> categoryBrandEntities = Arrays.asList(brandDTO.getCategory().split(","))
                    .stream().map(cid -> {
                        //新增
                        CategoryBrandEntity entity = new CategoryBrandEntity();

                        entity.setCategoryId(Integer.parseInt(cid));
                        entity.setBrandId(brandEntity.getId());
                        System.out.println(entity);
                        return entity;

                    }).collect(Collectors.toList());
            //批量新增
            categoryBrandMapper.insertList(categoryBrandEntities);

        } else {
            //新增
            CategoryBrandEntity entity = new CategoryBrandEntity();

            entity.setCategoryId(StringUtil.toInteger(brandDTO.getCategory()));
            entity.setBrandId(brandEntity.getId());

            categoryBrandMapper.insertSelective(entity);
        }
    }

    @Override
    public Result<JsonObject> deleteBrand(Integer id) {

        Example example = new Example(SpuEntity.class);
        example.createCriteria().andEqualTo("brandId", id);
        List<SpuEntity> list = spuMapper.selectByExample(example);
        if (list.size() == 0) {
            brandMapper.deleteByPrimaryKey(id);
            this.deleteCategoryBrand(id);
        } else {
            return this.setResultError("品牌被商品绑定不能被删除");
        }

        return this.setResultSuccess();
    }

    private void deleteCategoryBrand(Integer id) {
        //删除中间表得数据
        Example example = new Example(CategoryBrandEntity.class);
        example.createCriteria().andEqualTo("brandId", id);
        categoryBrandMapper.deleteByExample(example);
    }
}
