package com.baidu.web;

import com.baidu.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @ClassName PageController
 * @Description: TODO
 * @Author huangyanan
 * @Date 2020/9/23
 * @Version V1.0
 **/
//@Controller
//@RequestMapping(value = "/item")
public class PageController {

    //@Autowired
    private PageService pageService;

    //@GetMapping(value = "/{spuId}.html")
    public String test(@PathVariable(value = "spuId") Integer spuId, ModelMap map) {

        Map<String, Object> pageInfo = pageService.getPageInfoBySpuId(spuId);

        map.putAll(pageInfo);

        return "item";
    }
}
