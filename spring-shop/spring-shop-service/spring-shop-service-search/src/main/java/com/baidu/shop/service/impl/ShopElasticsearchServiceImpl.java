package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.dto.SpuDTO;

import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.response.GoodsResponse;
import com.baidu.shop.service.ShopElasticsearchService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.ESHighLightUtil;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ShopElasticsearchServiceImpl
 * @Description: TODO
 * @Author huangyanan
 * @Date 2020/9/16
 * @Version V1.0
 **/
@Slf4j
@RestController
public class ShopElasticsearchServiceImpl extends BaseApiService implements ShopElasticsearchService {
    @Resource
    private GoodsFeign goodsFeign;

    @Resource
    private SpecificationFeign specificationFeign;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private CategoryFeign categoryFeign;

    @Resource
    private BrandFeign brandFeign;


    @Override
    public GoodsResponse search(String search, Integer page, String filterStr) {

        if (StringUtil.isEmpty(search)) throw new RuntimeException("查询不能为空");

        SearchHits<GoodsDoc> hits = elasticsearchRestTemplate.search(this.getSearchQueryBuider(search, page, filterStr).build(), GoodsDoc.class);
        List<SearchHit<GoodsDoc>> highLightHit = ESHighLightUtil.getHighLightHit(hits.getSearchHits());

        //要返回的数据
        List<GoodsDoc> goodsList = highLightHit.stream().map(searchHit -> searchHit.getContent()).collect(Collectors.toList());

        //总条数/总页数
        long total = hits.getTotalHits();
        Long totalPage = Double.valueOf(Math.ceil(Long.valueOf(total).doubleValue() / 10)).longValue();

        Aggregations aggregations = hits.getAggregations();

        //获取分类和品牌的集合
        Map<Integer, List<CategoryEntity>> map = this.getCategoryList(aggregations);

        List<CategoryEntity> categoryList = null;
        Integer hotCid = 0;

        for (Map.Entry<Integer, List<CategoryEntity>> mapEntry : map.entrySet()) {
            hotCid = mapEntry.getKey();
            categoryList = mapEntry.getValue();
        }

        Map<String, List<String>> specParamValueMap = this.getSpecParam(hotCid, search);

        List<BrandEntity> brandList = this.getBrandList(aggregations);

        GoodsResponse goodsResponse = new GoodsResponse(total, totalPage, brandList, categoryList, goodsList, specParamValueMap);

        return goodsResponse;

    }

    private Map<String, List<String>> getSpecParam(Integer hotCid, String search) {
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(hotCid);
        specParamDTO.setSearching(true);

        Result<List<SpecParamEntity>> paramResult = specificationFeign.getParam(specParamDTO);
        if (paramResult.getCode() == 200) {
            List<SpecParamEntity> specParamList = paramResult.getData();
            //聚合查询
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            queryBuilder.withQuery(QueryBuilders.multiMatchQuery(search, "beandName", "categoryName", "title"));
            //分页必须得查询一个条件
            queryBuilder.withPageable(PageRequest.of(0, 1));

            specParamList.stream().forEach(specParam -> {
                queryBuilder.addAggregation(AggregationBuilders.terms(specParam.getName()).field("specs." + specParam.getName() + ".keyword"));
            });

            SearchHits<GoodsDoc> hits = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsDoc.class);

            //key:paramName value-->aggr
            Map<String, List<String>> map = new HashMap<>();
            Aggregations aggregations = hits.getAggregations();

            specParamList.stream().forEach(specParam -> {
                Terms term = aggregations.get(specParam.getName());
                List<? extends Terms.Bucket> buckets = term.getBuckets();
                List<String> valueList = buckets.stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
                map.put(specParam.getName(), valueList);
            });
            return map;
        }
        return null;

    }

    //构建条件查询
    private NativeSearchQueryBuilder getSearchQueryBuider(String search, Integer page, String filterStr) {

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        if (StringUtil.isNotEmpty(filterStr) && filterStr.length() > 2) {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            Map<String, String> filterMap = JSONUtil.toMapValueString(filterStr);

            filterMap.forEach((key, value) -> {
                MatchQueryBuilder matchQueryBuilder = null;
                if (key.equals("cid3") || key.equals("brandId")) {
                    matchQueryBuilder = QueryBuilders.matchQuery(key, value);
                } else {
                    matchQueryBuilder = QueryBuilders.matchQuery("specs." + key + ".keyword", value);
                }
                boolQueryBuilder.must(matchQueryBuilder);
            });
            queryBuilder.withFilter(boolQueryBuilder);
        }
        //多字段同时查询
        queryBuilder.withQuery(QueryBuilders.multiMatchQuery(search, "title", "brandName", "categoryName"));

        //过滤品牌 分类
        queryBuilder.addAggregation(AggregationBuilders.terms("cid_agg").field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms("brand_agg").field("brandId"));
        //高亮
        queryBuilder.withHighlightBuilder(ESHighLightUtil.getHighlightBuilder("title"));
        //分页
        queryBuilder.withPageable(PageRequest.of(page - 1, 10));
        return queryBuilder;
    }

    private List<BrandEntity> getBrandList(Aggregations aggregations) {
        Terms brand_agg = aggregations.get("brand_agg");

        List<String> brandList = brand_agg.getBuckets().stream().map(brandBucket -> {
            Number keyAsNumber = brandBucket.getKeyAsNumber();
            return keyAsNumber.intValue() + "";
        }).collect(Collectors.toList());

        String beandsStr = String.join(",", brandList);
        Result<List<BrandEntity>> brandRusult = brandFeign.getBeandbyIdList(beandsStr);
        return brandRusult.getData();

    }

    private Map<Integer, List<CategoryEntity>> getCategoryList(Aggregations aggregations) {
        Terms cid_agg = aggregations.get("cid_agg");

        List<? extends Terms.Bucket> cidBuckets = cid_agg.getBuckets();
        List<Integer> hotCidArr = Arrays.asList(0);
        List<Long> maxCount = Arrays.asList(0L);

        Map<Integer, List<CategoryEntity>> map = new HashMap<>();

        List<String> cidList = cidBuckets.stream().map(cidBucket -> {
            Number keyAsNumber = cidBucket.getKeyAsNumber();
            if (cidBucket.getDocCount() > maxCount.get(0)) {
                maxCount.set(0, cidBucket.getDocCount());
                hotCidArr.set(0, keyAsNumber.intValue());
            }
            return keyAsNumber.intValue() + "";
        }).collect(Collectors.toList());

        String cidsStr = String.join(",", cidList);

        Result<List<CategoryEntity>> categoryResult = categoryFeign.getCateByIdList(cidsStr);

        map.put(hotCidArr.get(0), categoryResult.getData());
        return map;
    }

    //初始化es数据
    @Override
    public Result<JSONObject> InitGoodsEsData() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if (!indexOperations.exists()) {
            indexOperations.create();
            log.info("创建索引成功");
            indexOperations.createMapping();
            log.info("映射索引成功");
        }
        //批量新增数据
        List<GoodsDoc> goodsDocs = this.esGoodsInfo();
        elasticsearchRestTemplate.save(goodsDocs);

        return this.setResultSuccess();
    }

    //清空es数据
    @Override
    public Result<JSONObject> clearGoodsEsData() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if (indexOperations.exists()) {
            indexOperations.delete();
            log.info("删除索引成功");
        }
        return this.setResultSuccess();
    }

    //获取mysql数据
    private List<GoodsDoc> esGoodsInfo() {

        //查询出来的数据是多个spu
        List<GoodsDoc> goodsDocs = new ArrayList<>();

        SpuDTO spuDTO = new SpuDTO();
        Result<List<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(spuDTO);
        if (spuInfo.getCode() == HTTPStatus.OK) {

            spuInfo.getData().stream().forEach(spu -> {
                GoodsDoc goodsDoc = new GoodsDoc();
                //spu信息填充
                goodsDoc.setId(spu.getId().longValue());
                goodsDoc.setCid1(spu.getCid1().longValue());
                goodsDoc.setCid2(spu.getCid2().longValue());
                goodsDoc.setCid3(spu.getCid3().longValue());
                goodsDoc.setCreateTime(spu.getCreateTime());
                goodsDoc.setSubTitle(spu.getSubTitle());
                goodsDoc.setBrandId(spu.getBrandId().longValue());
                //可搜锁数据
                goodsDoc.setTitle(spu.getTitle());
                goodsDoc.setBrandName(spu.getBrandName());
                goodsDoc.setCategoryName(spu.getCategoryName());

                //sku信息填充
                Map<List<Long>, List<Map<String, Object>>> specMap1 = this.getSpecMap(spu.getId());
                specMap1.forEach((key, value) -> {
                    goodsDoc.setPrice(key);
                    goodsDoc.setSkus(JSONUtil.toJsonString(value));
                });

                //通过cid3查询规格参数
                Map<String, Object> specMap = this.getSpecMap(spu);
                goodsDoc.setSpecs(specMap);
                goodsDocs.add(goodsDoc);
            });
        }
        return goodsDocs;
    }


    private Map<String, Object> getSpecMap(SpuDTO spu) {
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(spu.getCid3());
        Result<List<SpecParamEntity>> specParamResult = specificationFeign.getParam(specParamDTO);

        Map<String, Object> specMap = new HashMap<>();

        if (specParamResult.getCode() == HTTPStatus.OK) {
            //只有规格参数的id和规格参数的名字
            List<SpecParamEntity> paramList = specParamResult.getData();

            //通过spuid去查询spuDetail,detail里面有通用和特殊规格参数的值
            Result<SpuDetailEntity> spuDetailResult = goodsFeign.getSpuDetailBydSpuId(spu.getId());
            if (spuDetailResult.getCode() == HTTPStatus.OK) {
                SpuDetailEntity spuDetailInfo = spuDetailResult.getData();

                //通用规格参数的值
                String genericSpec = spuDetailInfo.getGenericSpec();
                Map<String, String> genericSpecMap = JSONUtil.toMapValueString(genericSpec);
                //特有规格参数的值
                String specialSpec = spuDetailInfo.getSpecialSpec();
                Map<String, List<String>> stringListMap = JSONUtil.toMapValueStrList(specialSpec);
                paramList.stream().forEach(param -> {
                    if (param.getGeneric()) {//是否是sku通用属性，true或false',

                        if (param.getSearching() && param.getSegments() != null) {
                            specMap.put(param.getName(), this.chooseSegment(genericSpecMap.get(param.getId() + ""), param.getSegments(), param.getUnit()));
                        } else {
                            specMap.put(param.getName(), genericSpecMap.get(param.getId() + ""));
                        }
                    } else {
                        specMap.put(param.getName(), stringListMap.get(param.getId() + ""));
                    }
                });
            }
        }
        return specMap;
    }

    /**
     * 把具体的值转换成区间-->不做范围查询
     *
     * @param value
     * @param segments
     * @param unit
     * @return
     */
    private String chooseSegment(String value, String segments, String unit) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : segments.split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + unit + "以上";
                } else if (begin == 0) {
                    result = segs[1] + unit + "以下";
                } else {
                    result = segment + unit;
                }
                break;
            }
        }
        return result;
    }

    private Map<List<Long>, List<Map<String, Object>>> getSpecMap(Integer SpuId) {
        Map<List<Long>, List<Map<String, Object>>> hashMap = new HashMap<>();

        Result<List<SkuDTO>> skuList = goodsFeign.getSkuBySpuId(SpuId);

        List<Map<String, Object>> skuMap = null;
        List<Long> priceList = new ArrayList<>();

        if (skuList.getCode() == HTTPStatus.OK) {
            skuMap = skuList.getData().stream().map(sku -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", sku.getId());
                map.put("title", sku.getTitle());
                map.put("images", sku.getImages());
                map.put("price", sku.getPrice());

                priceList.add(sku.getPrice().longValue());

                return map;

            }).collect(Collectors.toList());
        }
        hashMap.put(priceList, skuMap);
        return hashMap;
    }

}
