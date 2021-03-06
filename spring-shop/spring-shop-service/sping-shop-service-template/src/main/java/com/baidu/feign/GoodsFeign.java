package com.baidu.feign;

import com.baidu.shop.service.GoodsService;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @ClassName GoodsFeign
 * @Description: TODO
 * @Author huangyanan
 * @Date 2020/9/16
 * @Version V1.0
 **/
@FeignClient(contextId = "GoodsService", value = "xxx-service")
public interface GoodsFeign extends GoodsService {

}
