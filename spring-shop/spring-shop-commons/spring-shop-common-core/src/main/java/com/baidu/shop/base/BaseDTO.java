package com.baidu.shop.base;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * @ClassName BaseDTO
 * @Description: TODO
 * @Author huangyanan
 * @Date 2020/8/31
 * @Version V1.0
 **/
@ApiModel(value = "通用DTO")
public class BaseDTO {
    @ApiModelProperty(value = "当前页", example = "1")
    private Integer page;

    @ApiModelProperty(value = "每页显示多少条", example = "5")
    private Integer rows;

    @ApiModelProperty(value = "排序")
    private String sort;

    @ApiModelProperty(value = "是否降序")
    private Boolean desc;

    public String getOrderByClauser() {
        if (!StringUtils.isEmpty(sort)) return sort + " " + (desc ? "desc" : "asc");
        return null;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Boolean getDesc() {
        return desc;
    }

    public void setDesc(Boolean desc) {
        this.desc = desc;
    }
}
