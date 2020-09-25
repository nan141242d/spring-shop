package com.baidu.test;
import com.baidu.RunTestEsApplication;
import com.baidu.entity.GoodsEntity;
import com.baidu.repository.GoodsEsRepository;
import com.baidu.utils.ESHighLightUtil;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName TestEs
 * @Description: TODO
 * @Author huangyanan
 * @Date 2020/9/14
 * @Version V1.0
 **/
//让测试在Spring容器环境下执行
@RunWith(SpringRunner.class)
//声明启动类,当测试方法运行的时候会帮我们自动启动容器
@SpringBootTest(classes = { RunTestEsApplication.class})
public class TestEs {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private GoodsEsRepository goodsEsRepository;


    @Test
    public void createGoodsIndex(){
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(IndexCoordinates.of("indexName"));
        indexOperations.create();//创建索引

        System.out.println(indexOperations.exists()?"索引创建成功":"索引创建失败");
    }

    @Test
    public void createGoodsMapping(){

        //此构造函数会检查有没有索引存在,如果没有则创建该索引,如果有则使用原来的索引
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsEntity.class);

        indexOperations.createMapping();//创建映射,不调用此函数也可以创建映射,这就是高版本的强大之处
        System.out.println("映射创建成功");
    }

    @Test
    public void deleteIndex(){

        //此构造函数会检查有没有索引存在,如果没有则创建该索引,如果有则使用原来的索引
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsEntity.class);

        indexOperations.delete();//创建映射,不调用此函数也可以创建映射,这就是高版本的强大之处
        System.out.println("indexDELETE成功");
    }



    /*
新增文档
 */
    @Test
    public void saveData(){

        GoodsEntity entity = new GoodsEntity();
        entity.setId(1L);
        entity.setBrand("小米");
        entity.setCategory("手机");
        entity.setImages("xiaomi.jpg");
        entity.setPrice(1000D);
        entity.setTitle("小米3");

        goodsEsRepository.save(entity);

        System.out.println("新增成功");
    }


    /*
   批量新增文档
    */
    @Test
    public void saveAllData(){

        GoodsEntity entity = new GoodsEntity();
        entity.setId(2L);
        entity.setBrand("苹果");
        entity.setCategory("手机");
        entity.setImages("pingguo.jpg");
        entity.setPrice(5000D);
        entity.setTitle("iphone11手机");

        GoodsEntity entity2 = new GoodsEntity();
        entity2.setId(3L);
        entity2.setBrand("三星");
        entity2.setCategory("手机");
        entity2.setImages("sanxing.jpg");
        entity2.setPrice(3000D);
        entity2.setTitle("w2019手机");

        GoodsEntity entity3 = new GoodsEntity();
        entity3.setId(4L);
        entity3.setBrand("华为");
        entity3.setCategory("手机");
        entity3.setImages("huawei.jpg");
        entity3.setPrice(4000D);
        entity3.setTitle("华为mate30手机");

        goodsEsRepository.saveAll(Arrays.asList(entity,entity2,entity3));

        System.out.println("批量新增成功");
    }

    /*
   更新文档
    */
    @Test
    public void updateData(){

        GoodsEntity entity = new GoodsEntity();
        entity.setId(1L);
        entity.setBrand("小米");
        entity.setCategory("手机");
        entity.setImages("xiaomi.jpg");
        entity.setPrice(1000D);
        entity.setTitle("小米3");

        goodsEsRepository.save(entity);

        System.out.println("修改成功");
    }

    /*
  删除文档
   */
    @Test
    public void delData(){

        GoodsEntity entity = new GoodsEntity();
        entity.setId(1L);

        goodsEsRepository.delete(entity);

        System.out.println("删除成功");
    }

    /*
   查询所有
    */
    @Test
    public void searchAll(){
        long count = goodsEsRepository.count();
        System.out.println(count);
        Iterable<GoodsEntity> all = goodsEsRepository.findAll();
        all.forEach(goods -> System.out.println(goods));
    }
/*
* 条件查询
* */
    @Test
    public void searchByParam(){
        List<GoodsEntity> title = goodsEsRepository.findByTitle("华为手机");
        System.out.println(title);

        List<GoodsEntity> byAndPriceBetween = goodsEsRepository.findByAndPriceBetween(1000D, 3000D);
        System.out.println(byAndPriceBetween);
    }
    /*
    * 自定义查询*/

    @Test
    public void customizeSearch(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("title","华为"))
                .must(QueryBuilders.rangeQuery("price").gt(1000D).lt(3000D))
        );

    }

    /*
    * 高亮
    * */
    @Test
    public void customizeSearchHighLight(){
        //构建高级查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //构建高亮查询
        HighlightBuilder builder = new HighlightBuilder();
        //设置高亮字段  Field是HighlightBuilder里的静态内部类
        HighlightBuilder.Field title = new HighlightBuilder.Field("title");
        //设置高亮标签
        title.preTags("<font style='color:red'>");
        title.postTags("</font>");
        builder.field(title);
        //高亮查询
        queryBuilder.withHighlightBuilder(builder);

        queryBuilder.withQuery(
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("title","小米手机"))
        );

        SearchHits<GoodsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsEntity.class);

        List<SearchHit<GoodsEntity>> searchHits = search.getSearchHits();

        List<GoodsEntity> goodsList = searchHits.stream().map(hit -> {

            GoodsEntity entity = hit.getContent();
            //通过字段获取高亮查询结果
            List<String> title1 = hit.getHighlightField("title");
            entity.setTitle(title1.get(0));
            System.out.println(title1);
            return entity;
        }).collect(Collectors.toList());

        goodsList.stream().forEach(goods -> System.out.println(goods));
    }

    @Test
    public void customizeSearchHighLightUtil(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //构建高亮查询
        queryBuilder.withHighlightBuilder(ESHighLightUtil.getHighlightBuilder("title"));//设置高亮
        queryBuilder.withQuery(
                QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("title","华为手机"))
                .must(QueryBuilders.rangeQuery("price").gte(1000).lte(10000))
        );
        SearchHits<GoodsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsEntity.class);
        List<SearchHit<GoodsEntity>> searchHits = search.getSearchHits();

        List<SearchHit<GoodsEntity>> highLightHit = ESHighLightUtil.getHighLightHit(searchHits);
        System.out.println(highLightHit);
    }

    @Test
    public void searchAgg(){

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.addAggregation(
                AggregationBuilders.terms("brand_agg").field("brand")
        );

        SearchHits<GoodsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsEntity.class);

        Aggregations aggregations = search.getAggregations();

        //terms 是Aggregation的子类
        //Aggregation brand_agg = aggregations.get("brand_agg");/
        Terms terms = aggregations.get("brand_agg");

        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        buckets.forEach(bucket -> {
            System.out.println(bucket.getKeyAsString() + ":" + bucket.getDocCount());
        });
        System.out.println(search);
    }
    @Test
    public void searchAggMethod(){

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.addAggregation(
                AggregationBuilders.terms("brand_agg")
                        .field("brand")
                        //聚合函数
                        .subAggregation(AggregationBuilders.max("max_price").field("price"))
        );

        SearchHits<GoodsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsEntity.class);

        Aggregations aggregations = search.getAggregations();

        Terms terms = aggregations.get("brand_agg");

        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        buckets.forEach(bucket -> {
            System.out.println(bucket.getKeyAsString() + ":" + bucket.getDocCount());

            //获取聚合
            Aggregations aggregations1 = bucket.getAggregations();
            //得到map
            Map<String, Aggregation> map = aggregations1.asMap();
            //需要强转,Aggregations是一个类 Terms是他的子类,Aggregation是一个接口Max是他的子接口,而且Max是好几个接口的子接口
            Max max_price = (Max) map.get("max_price");
            System.out.println(max_price.getValue());
        });
    }





}
