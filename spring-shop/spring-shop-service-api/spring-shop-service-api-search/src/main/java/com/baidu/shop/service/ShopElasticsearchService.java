package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @ClassName ShopElasticsearchService
 * @Description: TODO
 * @Author huangyanan
 * @Date 2020/9/16
 * @Version V1.0
 **/
@Api(tags = "es接口")
public interface ShopElasticsearchService {

    @ApiOperation(value = "初始化es数据-->创建索引，创建映射，mysql数据同步")
    @GetMapping(value = "es/InitGoodsEsData")
    Result<JSONObject> InitGoodsEsData();

    @ApiOperation(value = "删除索引")
    @GetMapping(value = "es/clearGoodsEsData")
    Result<JSONObject> clearGoodsEsData();

    @ApiOperation(value = "查询数据")
    @GetMapping(value = "es/search")
    Result<List<GoodsDoc>> search(@RequestParam String search, @RequestParam Integer page, String filterStr);

}
