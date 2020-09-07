package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecGroupDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Delete;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "规格接口")
public interface SpecificationService {

    @ApiOperation(value = "通过条件查询规格组")
    @GetMapping(value = "specgroup/getGroup")
    Result<List<SpecGroupEntity>> getGroup(SpecGroupDTO specGroupDTO);

    @ApiOperation(value = "新增规格组")
    @PostMapping(value = "specgroup/save")
    Result<JSONObject> add(@Validated({MingruiOperation.Add.class}) @RequestBody SpecGroupDTO specGroupDTO);

    @ApiOperation(value = "新增规格组数据")
    @PutMapping(value = "specgroup/save")
    Result<JSONObject> edit(@Validated({MingruiOperation.Update.class}) @RequestBody SpecGroupDTO specGroupDTO);

    @ApiOperation(value = "删除规格组数据")
    @DeleteMapping(value = "specgroup/delete")
    Result<JSONObject> delete(Integer id);

    @ApiOperation(value = "通过条件查询参数")
    @GetMapping(value = "specParam/getParam")
    Result<List<SpecParamEntity>> getParam(SpecParamDTO specParamDTO);

    @ApiOperation(value = "新增参数组")
    @PostMapping(value = "specParam/saveParam")
    Result<JSONObject> saveParam(@Validated({MingruiOperation.Add.class}) @RequestBody SpecParamDTO specParamDTO);

    @ApiOperation(value = "修改参数组")
    @PutMapping(value = "specParam/saveParam")
    Result<JSONObject> editParam(@Validated({MingruiOperation.Update.class}) @RequestBody SpecParamDTO specParamDTO);

    @ApiOperation(value = "删除参数组数据")
    @DeleteMapping(value = "specParam/delete")
    Result<JSONObject> deleteParam(Integer id);

}
