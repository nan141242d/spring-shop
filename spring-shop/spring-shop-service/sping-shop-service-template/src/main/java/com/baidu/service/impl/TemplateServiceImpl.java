package com.baidu.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.feign.CategoryFeign;
import com.baidu.feign.GoodsFeign;
import com.baidu.feign.SpecificationFeign;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpecGroupDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.service.TemplateService;
import com.baidu.shop.utils.BaiduBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

/**
 * @ClassName TemplateServiceImpl
 * @Description: TODO
 * @Author huangyanan
 * @Date 2020/9/25
 * @Version V1.0
 **/
@RestController
public class TemplateServiceImpl extends BaseApiService implements TemplateService {

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private SpecificationFeign specificationFeign;

    //注入静态化模板
    @Autowired
    private TemplateEngine templateEngine;

    @Value(value = "${mrshop.static.html.path}")
    private String staticHTMLPath;


    @Override
    public Result<JSONObject> delHTMLBySpuId(Integer spuId) {
        File file = new File(staticHTMLPath + File.separator + spuId + ".html");

        if (!file.delete()) {
            return this.setResultError("文件删除失败");
        }

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> createStaticHTMLTemplate(Integer spuId) {

        //现在可以创建上下文了
        Map<String, Object> map = this.getPageInfoBySpuId(spuId);
        //创建模板引擎上下文
        Context context = new Context();
        //将准备好的数据放到模板中
        context.setVariables(map);

        //创建文件路径
        File file = new File(staticHTMLPath, spuId + ".html");
        //创建文件书输出流
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            templateEngine.process("item", context, writer);
            writer.close();//释放资源
        }


        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> initStaticHTMLTemplate() {

        //获取所有spu数据
        Result<List<SpuDTO>> spuInfoResult = goodsFeign.getSpuInfo(new SpuDTO());
        if (spuInfoResult.getCode() == 200) {
            List<SpuDTO> spuDtoList = spuInfoResult.getData();
            spuDtoList.stream().forEach(spuDTO -> {
                createStaticHTMLTemplate(spuDTO.getId());
            });
        }
        return this.setResultSuccess();
    }

    private Map<String, Object> getPageInfoBySpuId(Integer spuId) {

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
