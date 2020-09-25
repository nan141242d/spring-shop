package com.baidu.shop.service;

import com.baidu.shop.dto.BrandDTO;

import com.baidu.shop.base.Result;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Api(tags = "品牌管理接口")
public interface BrandService {
    @ApiOperation(value = "查询品牌")
    @GetMapping(value = "brand/list")
    public Result<PageInfo<BrandEntity>> getList(BrandDTO brandDTO);

    @ApiOperation(value = "新增品牌")
    @PostMapping(value = "brand/save")
    Result<JsonObject> saveBrand(@Validated({MingruiOperation.Add.class}) @RequestBody BrandDTO brandDTO);

    @ApiOperation(value = "修改品牌")
    @PutMapping(value = "brand/save")
    Result<JsonObject> editBrand(@Validated({MingruiOperation.Update.class}) @RequestBody BrandDTO brandDTO);

    @ApiOperation(value = "删除品牌")
    @DeleteMapping(value = "brand/delete")
    Result<JsonObject> deleteBrand(Integer id);


    @ApiOperation(value = "根据分类查询品牌")
    @GetMapping(value = "brand/getBrandByCategory")
    public Result<PageInfo<BrandEntity>> getBrandByCate(Integer cid);

    @ApiOperation(value = "根据分类查询品牌")
    @GetMapping(value = "brand/getBeandbyIdList")
    Result<List<BrandEntity>> getBeandbyIdList(@RequestParam String brandsStr);
}
