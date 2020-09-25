package com.baidu.service.impl;

import com.baidu.feign.BrandFeign;
import com.baidu.feign.CategoryFeign;
import com.baidu.feign.GoodsFeign;
import com.baidu.feign.SpecificationFeign;
import com.baidu.service.PageService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.*;
import com.baidu.shop.entity.*;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName PageServiceImpl
 * @Description: TODO
 * @Author huangyanan
 * @Date 2020/9/23
 * @Version V1.0
 **/
//@Service
public class PageServiceImpl implements PageService {

    //@Autowired
    private BrandFeign brandFeign;

    // @Autowired
    private GoodsFeign goodsFeign;

    // @Autowired
    private CategoryFeign categoryFeign;

    // @Autowired
    private SpecificationFeign specificationFeign;

    @Override
    public Map<String, Object> getPageInfoBySpuId(Integer spuId) {

        Map<String, Object> map = new HashMap<>();

        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);
        Result<List<SpuDTO>> spuInfoResult = goodsFeign.getSpuInfo(spuDTO);
        if (spuInfoResult.getCode() == 200) {
            List<SpuDTO> data = spuInfoResult.getData();
            if (data.size() == 1) {
                //spu信息
                SpuDTO spuInfo = data.get(0);
                map.put("spuInfo", spuInfo);
                //分类信息
                Result<List<CategoryEntity>> cateByIdListResult = categoryFeign.getCateByIdList(String.join(",", spuInfo.getCid1() + "", spuInfo.getCid2() + "", spuInfo.getCid3() + ""));
                if (cateByIdListResult.getCode() == 200) map.put("category", cateByIdListResult.getData());
                //sku
                Result<List<SkuDTO>> skuResult = goodsFeign.getSkuBySpuId(spuId);
                if (skuResult.getCode() == 200) map.put("skus", skuResult.getData());
                //特有参数信息和值
                SpecParamDTO specParamDTO = new SpecParamDTO();
                specParamDTO.setCid(spuInfo.getCid3());
                specParamDTO.setGeneric(false);
                Result<List<SpecParamEntity>> specParamResult = specificationFeign.getParam(specParamDTO);
                if (specParamResult.getCode() == 200) {
                    Map<Integer, String> specMap = new HashMap<>();
                    specParamResult.getData().stream().forEach(specParamEntity -> {
                        specMap.put(specParamEntity.getId(), specParamEntity.getName());
                        map.put("paramMap", specMap);
                    });
                }
                //spuDetail
                Result<SpuDetailEntity> spuDetailResult = goodsFeign.getSpuDetailBydSpuId(spuId);
                if (spuDetailResult.getCode() == 200) map.put("spuDetail", spuDetailResult.getData());

                //规格参数
                SpecGroupDTO specGroupDTO = new SpecGroupDTO();
                specGroupDTO.setCid(spuInfo.getCid3());
                Result<List<SpecGroupEntity>> groupResult = specificationFeign.getGroup(specGroupDTO);
                if (groupResult.getCode() == 200) {
                    List<SpecGroupEntity> groupEntityList = groupResult.getData();
                    List<SpecGroupDTO> specGroupDtoList = groupEntityList.stream().map(specGroupEntity -> {
                        SpecGroupDTO specGroup = BaiduBeanUtil.copyProperties(specGroupEntity, SpecGroupDTO.class);
                        //通用param
                        SpecParamDTO specParamDTO1 = new SpecParamDTO();
                        specParamDTO1.setGroupId(specGroup.getId());
                        specParamDTO1.setGeneric(true);
                        Result<List<SpecParamEntity>> paramResult = specificationFeign.getParam(specParamDTO1);

                        if (paramResult.getCode() == 200) {
                            specGroup.setParamList(paramResult.getData());
                        }
                        return specGroup;
                    }).collect(Collectors.toList());
                    map.put("specGroupDtoList", specGroupDtoList);

                }
            }

        }
        return map;
    }
}
