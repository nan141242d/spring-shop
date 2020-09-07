package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecGroupDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.mapper.ParamMapper;
import com.baidu.shop.mapper.SpecGroupMapper;
import com.baidu.shop.service.SpecificationService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName SpecificationServiceImpl
 * @Description: TODO
 * @Author huangyanan
 * @Date 2020/9/3
 * @Version V1.0
 **/
@RestController
public class SpecificationServiceImpl extends BaseApiService implements SpecificationService {

    @Resource
    private SpecGroupMapper specGroupMapper;

    @Resource
    private ParamMapper paramMapper;

    @Override
    public Result<List<SpecGroupEntity>> getGroup(SpecGroupDTO specGroupDTO) {
        //通过分类id查询数据
        Example example = new Example(SpecGroupEntity.class);

        if (ObjectUtil.isNotNull(specGroupDTO.getCid())) ;
        example.createCriteria().andEqualTo("cid", specGroupDTO.getCid());

        List<SpecGroupEntity> list = specGroupMapper.selectByExample(example);
        return this.setResultSuccess(list);

//        Example example = new Example(SpecGroupEntity.class);
//
//        if(ObjectUtil.isNotNull(specGroupDTO.getCid())) example.createCriteria().andEqualTo("cid",specGroupDTO.getCid());
//
//        List<SpecGroupEntity> list = specGroupMapper.selectByExample(example);
//
//        return this.setResultSuccess(list);
    }

    @Override
    public Result<JSONObject> add(SpecGroupDTO specGroupDTO) {

        specGroupMapper.insertSelective(BaiduBeanUtil.copyProperties(specGroupDTO, SpecGroupEntity.class));

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> edit(SpecGroupDTO specGroupDTO) {

        specGroupMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(specGroupDTO, SpecGroupEntity.class));

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> delete(Integer id) {

        /*List<CategoryEntity> bycId = specGroupMapper.getBycId(id);
        if (bycId.size() == 0){
            specGroupMapper.deleteByPrimaryKey(id);
            return this.setResultSuccess();
        }*/
        Example example = new Example(SpecParamEntity.class);
        example.createCriteria().andEqualTo("groupId", id);
        List<SpecParamEntity> list = paramMapper.selectByExample(example);
        //if(list.size() == 0 ){  specGroupMapper.deleteByPrimaryKey(id); return this.setResultSuccess();}
        if (list.size() > 0) return this.setResultError("规则组包含参数不能删除");

        specGroupMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();
    }


    @Override
    public Result<List<SpecParamEntity>> getParam(SpecParamDTO specParamDTO) {
        if (ObjectUtil.isNull(specParamDTO.getGroupId())) return this.setResultError("规格组id不能为空");

        Example example = new Example(SpecParamEntity.class);
        example.createCriteria().andEqualTo("groupId", specParamDTO.getGroupId());

        List<SpecParamEntity> list = paramMapper.selectByExample(example);


        return this.setResultSuccess(list);
    }

    @Transactional
    @Override
    public Result<JSONObject> saveParam(SpecParamDTO specParamDTO) {

        paramMapper.insertSelective(BaiduBeanUtil.copyProperties(specParamDTO, SpecParamEntity.class));

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> editParam(SpecParamDTO specParamDTO) {

        paramMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(specParamDTO, SpecParamEntity.class));

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> deleteParam(Integer id) {

        paramMapper.deleteByPrimaryKey(id);

        return this.setResultSuccess();
    }
}
