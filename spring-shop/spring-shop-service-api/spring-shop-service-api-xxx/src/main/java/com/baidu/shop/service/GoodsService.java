package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.SpuDetailEntity;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsService {

    @ApiOperation(value = "获取spu信息")
    @GetMapping(value = "goods/getSpuInfo")
    Result<List<SpuDTO>> getSpuInfo(@SpringQueryMap SpuDTO spuDTO);

    @ApiOperation(value = "保存商品信息")
    @PostMapping(value = "goods/save")
    Result<JSONObject> save(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "修改商品信息")
    @PutMapping(value = "goods/save")
    Result<JSONObject> edit(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "获取sku信息")
    @GetMapping(value = "goods/getSkuBySpuId")
    Result<List<SkuDTO>> getSkuBySpuId(@RequestParam Integer spuId);

    @ApiOperation(value = "获取spu详细信息")
    @GetMapping(value = "goods/getSpuDetailBydSpu")
    Result<SpuDetailEntity> getSpuDetailBydSpuId(@RequestParam Integer spuId);

    @ApiOperation(value = "删除商品信息")
    @DeleteMapping(value = "goods/del")
    Result<JSONObject> delSpuBydSpuId(@RequestParam Integer spuId);


    @ApiOperation(value = "修改上架状态")
    @PutMapping(value = "goods/updateSaleable")
    Result<JSONObject> updateSaleable(@RequestBody SpuDTO spuDTO);


}
