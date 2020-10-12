package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;

import com.baidu.shop.component.MrRabbitMQ;
import com.baidu.shop.constant.MqMessageConstant;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.*;
import com.baidu.shop.mapper.*;

import com.baidu.shop.service.GoodsService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName GoodsServiceImpl
 * @Description: TODO
 * @Author huangyanan
 * @Date 2020/9/7
 * @Version V1.0
 **/
@RestController
public class GoodsServiceImpl extends BaseApiService implements GoodsService {

    @Resource
    private SpuMapper spuMapper;

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private SpuDetailMapper spuDetailMapper;

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private StockMapper stockMapper;

    @Autowired
    private MrRabbitMQ mrRabbitMQ;


    @Transactional
    @Override
    public Result<JSONObject> updateSaleable(SpuDTO spuDTO) {
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setId(spuDTO.getId());
        if (spuDTO.getSaleable() == 1) {
            spuEntity.setSaleable(0);
        } else {
            spuEntity.setSaleable(1);
        }
        spuMapper.updateByPrimaryKeySelective(spuEntity);
        return this.setResultSuccess();
    }

    //@Transactional
    @Override
    public Result<JSONObject> edit(SpuDTO spuDTO) {
        this.editTransaction(spuDTO);

        mrRabbitMQ.send(spuDTO.getId() + "", MqMessageConstant.SPU_ROUT_KEY_UPDATE);

        return this.setResultSuccess();
    }


    @Transactional
    public void editTransaction(SpuDTO spuDTO) {
        //修改spu
        Date date = new Date();
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setLastUpdateTime(date);
        spuMapper.updateByPrimaryKeySelective(spuEntity);

        //修改spuDetail
        spuDetailMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(), SpuDetailEntity.class));

        //查询skuIdlist
        //修改sku
        //修改stock
        this.delSkuAndStock(spuDTO.getId());

        //新增sku stock
        this.addSkuAndStock(spuDTO.getSkus(), spuDTO.getId(), date);
    }

    @Override
    public Result<List<SkuDTO>> getSkuBySpuId(Integer spuId) {
        List<SkuDTO> list = skuMapper.selectSkuAndStockBySpuId(spuId);
        return this.setResultSuccess(list);
    }

    @Override
    public Result<SpuDetailEntity> getSpuDetailBydSpuId(Integer spuId) {
        SpuDetailEntity spuDetailEntity = spuDetailMapper.selectByPrimaryKey(spuId);

        return this.setResultSuccess(spuDetailEntity);
    }

    //@Transactional
    @Override
    public Result<JSONObject> save(SpuDTO spuDTO) {
        Integer spuId = addInfoTransaction(spuDTO);

        mrRabbitMQ.send(spuId + "", MqMessageConstant.SPU_ROUT_KEY_SAVE);

        return this.setResultSuccess();
    }


    @Transactional
    public Integer addInfoTransaction(SpuDTO spuDTO) {
        //新增spu
        Date date = new Date();

        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setSaleable(1);
        spuEntity.setValid(1);
        spuEntity.setCreateTime(date);
        spuEntity.setLastUpdateTime(date);
        spuMapper.insertSelective(spuEntity);

        Integer spuId = spuEntity.getId();//根据spuId新增数据

        //新增spuDetail
        SpuDetailEntity spuDetailEntity = BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(), SpuDetailEntity.class);
        spuDetailEntity.setSpuId(spuId);
        spuDetailMapper.insertSelective(spuDetailEntity);

        //新增sku  根据skuId新增数据
        this.addSkuAndStock(spuDTO.getSkus(), spuId, date);

        return spuEntity.getId();

    }

    @Override
    public Result<List<SpuDTO>> getSpuInfo(SpuDTO spuDTO) {
        //分页
        if (ObjectUtil.isNotNull(spuDTO.getPage()) && ObjectUtil.isNotNull(spuDTO.getRows()))
            PageHelper.startPage(spuDTO.getPage(), spuDTO.getRows());

        //构建条件查询
        Example example = new Example(SpuEntity.class);
        Example.Criteria criteria = example.createCriteria();

        if (ObjectUtil.isNotNull(spuDTO)) {
            //标题
            if (StringUtil.isNotEmpty(spuDTO.getTitle()))
                criteria.andLike("title", spuDTO.getTitle());
            //是否上架
            if (ObjectUtil.isNotNull(spuDTO.getSaleable()) && spuDTO.getSaleable() != 2)
                criteria.andEqualTo("saleable", spuDTO.getSaleable());
            if (ObjectUtil.isNotNull(spuDTO.getId())) {
                criteria.andEqualTo("id", spuDTO.getId());
            }
            if (ObjectUtil.isNotNull(spuDTO.getSort()))
                example.setOrderByClause(spuDTO.getOrderByClauser());

        }
        //执行查询
        List<SpuEntity> list = spuMapper.selectByExample(example);
        //品牌
        List<Object> spuDtoList = list.stream().map(spuEntity -> {
            SpuDTO spuDTO1 = BaiduBeanUtil.copyProperties(spuEntity, SpuDTO.class);
            BrandEntity brandEntity = brandMapper.selectByPrimaryKey(spuEntity.getBrandId());
            if (ObjectUtil.isNotNull(brandEntity)) spuDTO1.setBrandName(brandEntity.getName());

            //分类
            String caterogyName = categoryMapper.selectByIdList(
                    Arrays.asList(spuDTO1.getCid1(), spuDTO1.getCid2(), spuDTO1.getCid3()))
                    .stream().map(category -> category.getName()).collect(Collectors.joining("/"));
            spuDTO1.setCategoryName(caterogyName);

            return spuDTO1;
        }).collect(Collectors.toList());

        //总条数
        PageInfo<SpuEntity> pageInfo = new PageInfo<>(list);

        return this.setResult(HTTPStatus.OK, pageInfo.getTotal() + "", spuDtoList);
    }

    //@Transactional
    @Override
    public Result<JSONObject> delSpuBydSpuId(Integer spuId) {
        this.delTransaction(spuId);

        mrRabbitMQ.send(spuId + "", MqMessageConstant.SPU_ROUT_KEY_DELETE);

        return this.setResultSuccess();
    }


    @Transactional
    public void delTransaction(Integer spuId) {
        //删除spu
        spuMapper.deleteByPrimaryKey(spuId);
        //删除spudetail
        spuDetailMapper.deleteByPrimaryKey(spuId);
        //删除sku stock
        this.delSkuAndStock(spuId);
    }

    //封装新增sku/stock
    public void addSkuAndStock(List<SkuDTO> skus, Integer spuId, Date date) {
        skus.stream().forEach(skuDTO -> {
            SkuEntity skuEntity = BaiduBeanUtil.copyProperties(skuDTO, SkuEntity.class);
            skuEntity.setSpuId(spuId);
            skuEntity.setCreateTime(date);
            skuEntity.setLastUpdateTime(date);
            skuMapper.insertSelective(skuEntity);

            //新增stock
            StockEntity stockEntity = new StockEntity();
            stockEntity.setSkuId(skuEntity.getId());
            stockEntity.setStock(skuDTO.getStock());
            stockMapper.insertSelective(stockEntity);

        });
    }

    public void delSkuAndStock(Integer spuId) {
        Example example = new Example(SkuEntity.class);
        example.createCriteria().andEqualTo("spuId", spuId);
        List<SkuEntity> skuEntities = skuMapper.selectByExample(example);
        List<Long> skuIdArr = skuEntities.stream().map(skuEntity -> skuEntity.getId()).collect(Collectors.toList());
        if (skuIdArr.size() > 0) {

            //删除skus
            skuMapper.deleteByIdList(skuIdArr);
            //删除stock
            stockMapper.deleteByIdList(skuIdArr);
        }
    }
}
