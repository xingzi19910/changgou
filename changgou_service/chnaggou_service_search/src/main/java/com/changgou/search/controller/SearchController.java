package com.changgou.search.controller;

import com.changgou.entity.Page;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SearchService;
import com.changgou.search.service.SkuSearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/sku_search")
public class SearchController {
    @Autowired
    private SkuSearchService skuSearchService;
    @Autowired
    private SearchService searchService;
    //对搜索参数带有特殊符号进行处理
    public void handlerSearchMap(Map<String,String> searchMap){
        if (searchMap!=null){
           // 得到键值对集合
            Set<Map.Entry<String, String>> entries = searchMap.entrySet();
            //遍历
            for (Map.Entry<String, String> entry : entries) {
                if (entry.getKey().startsWith("spec_")){
                    searchMap.put(entry.getKey(),entry.getValue().replace("+","%2B"));
                }
            }
        }
    }
    @GetMapping
    @ResponseBody
    public Map search(@RequestParam Map<String,String> map){
        //特殊符号处理
        handlerSearchMap(map);
        Map<String, Object> map1 = searchService.findByKeyWord(map);
        return map1;
    }

    @GetMapping("/list")
    public String list(@RequestParam Map<String,String> map, Model model){
        //特殊符号处理
        handlerSearchMap(map);
        Map<String, Object> resultMap = searchService.findByKeyWord(map);
        //携带数据 跳转页面
        model.addAttribute("resultMap",resultMap);
        model.addAttribute("searchMap",map);
        //后端拼接url
        StringBuilder url = new StringBuilder("/sku_search/list");
        if (map!=null&&map.size()>0){
            //http://localhost:9009/sku_search/list?keywords=手机
            url.append("?");
            for (String paramkey : map.keySet()) {
                //如果参数里不是 排序规则 排序字段 页数 拼接路径
                if (!"sortRule".equals(paramkey)&&!"sortField".equals(paramkey)&&!"pageNum".equals(paramkey)){
                    url.append(paramkey).append("=").append(map.get(paramkey)).append("&");
                }
            }

            String stringUrl = url.toString();
            //去掉最后一个&
            stringUrl = stringUrl.substring(0,stringUrl.length()-1);
            model.addAttribute("url",stringUrl);
        }else {
            model.addAttribute("url",url);
        }
        //数据分页查询
        Page<SkuInfo> page = new Page<SkuInfo>(Long.parseLong(resultMap.get("total").toString()),
        Integer.parseInt(resultMap.get("pageNum").toString()),
        Page.pageSize);
        //总记录数

        model.addAttribute("page",page);
        return "search";
    }


}
