package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;

import com.alibaba.fastjson.JSONObject;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.util.*;
import java.util.stream.Collectors;

//商品搜索
@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    //设置每页查询条数
    public final static Integer PAGE_SIZE = 20;

    //根据关键字查询
    @Override
    public Map<String, Object> findByKeyWord(Map<String, String> searchMap) {
        //5.封装查询结果
        Map<String, Object> resultMap = new HashMap<>();
        if (searchMap != null) {
            //2.构建查询条件封装对象    原生搜索实现类
            NativeSearchQueryBuilder nativeSearchQuery = new NativeSearchQueryBuilder();
            //组合条件对象
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            //4.关键词查询
            if (!StringUtils.isEmpty(searchMap.get("keywords"))) {
                //  matchQuery -->模糊的匹配查询   operator条件拼接
                boolQueryBuilder.must(QueryBuilders.matchQuery("name", searchMap.get("keywords")).operator(Operator.AND));
            }
            //按照品牌进行过滤查询   termQuery精确匹配
            if (!StringUtils.isEmpty(searchMap.get("brand"))) {
                // 参数1 域名   参数2 操作的值
                boolQueryBuilder.filter(QueryBuilders.termQuery("brandName", searchMap.get("brand")));
            }
            //按照品牌进行聚合查询 分组查询
            //定义变量skubrand
            String skuBrand = "brand";
            //构建查询条件内容
            NativeSearchQueryBuilder brandName = nativeSearchQuery.addAggregation(AggregationBuilders.terms(skuBrand).field("brandName"));
            //按照规格进行过滤查询 遍历所有的key
            for (String key : searchMap.keySet()) {
                if (key.startsWith("spec_")) {
                    //对内容进行转换
                    String value = searchMap.get(key).replace("%2B", "+");
                    //设置过滤条件
                    boolQueryBuilder.filter(QueryBuilders.termQuery(("specMap." + key.substring(5) + ".keyword"), value));
                }
            }

            //按照规格进行聚合查询
            //1.定义变量
            String skuSpec = "skuSpec";
            //构建查询条件内容
            NativeSearchQueryBuilder speckeyword = nativeSearchQuery.addAggregation(AggregationBuilders.terms(skuSpec).field("spec.keyword"));

            //3.根据条件查询
            nativeSearchQuery.withQuery(boolQueryBuilder);

            //价格区间搜索
            //1.先判断价格字段
            if (searchMap.get("price") != null) {
                String[] prices = searchMap.get("price").split("-");
                if (prices.length == 2) {
                    //设置过滤条件
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(prices[1]));
                }
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(prices[0]));
            }

                //分页查询
            String pageNum=searchMap.get("pageNum");
            if (pageNum==null){
                pageNum="1";
            }
            nativeSearchQuery.withPageable(PageRequest.of(Integer.parseInt(pageNum)-1,PAGE_SIZE));
                //排序查询
                if (!StringUtils.isEmpty(searchMap.get("sortField"))){
                    if ("ASC".equals(searchMap.get("sortRule"))){
                        nativeSearchQuery.withSort(SortBuilders.fieldSort(searchMap.get("sortField")).order(SortOrder.ASC));
                    }else {
                        nativeSearchQuery.withSort(SortBuilders.fieldSort(searchMap.get("sortField")).order(SortOrder.DESC));
                    }
                }
                //高亮域 以及高亮的样式
            HighlightBuilder.Field field = new HighlightBuilder.Field("name")    //高亮域
                    .preTags("<span style='color:red'>")   //高亮显示的前缀
                    .postTags("</span>"); //高亮显示的后缀
                    nativeSearchQuery.withHighlightFields(field);

            //1.执行查询方法 //参数1 构建查询对象,参数2 查询操作实体类 参数3 查询结果操作对象
            AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(nativeSearchQuery.build(), SkuInfo.class, new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                    //6.获取查询命中结果数据
                    SearchHits hits = response.getHits();
                    List<T> list = new ArrayList<>();
                    if (hits != null) {
                        for (SearchHit hit : hits) {
                            //7.将hit的JSON字符串转换成Sku
                            SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);
                            //8.封装最终的返回结果

                            //得到高亮域
                            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                            if (highlightFields!=null &&highlightFields.size()>0){
                                //替换数据
                                skuInfo.setName(highlightFields.get("name").getFragments()[0].toString());
                            }
                            list.add((T) skuInfo);
                        }
                    }
                    return new AggregatedPageImpl<T>(list, pageable, hits.getTotalHits(), response.getAggregations());
                }
            });

            //封装最终的返回结果
            //9.总条数
            resultMap.put("total", skuInfos.getTotalElements());

            //10总页数
            resultMap.put("totalPages", skuInfos.getTotalPages());

            //11.数据集合
            resultMap.put("rows", skuInfos.getContent());

            //封装品牌的分组结果
            //获取到聚合结果
            StringTerms brandTerms = (StringTerms) skuInfos.getAggregation(skuBrand);
            // System.out.println(brandTerms);
            //将map封装为集合 基于流运算  getBuckets 得到相对应结果 stream开启流  collect(toList) 转成list 品牌集合
            List<String> brandList = brandTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            resultMap.put("brandList", brandList);


           /*   List<StringTerms.Bucket> buckets = brandTerms.getBuckets();
            System.out.println(buckets);

            List list = new ArrayList();
            for (StringTerms.Bucket bucket : buckets) {
                // System.out.println(bucket);
                String keyAsString = bucket.getKeyAsString();

                list.add(keyAsString);

                System.out.println(keyAsString);
            }

            resultMap.put("brand",list);

           return resultMap;*/


            //封装规格分组结果
            StringTerms specTerms = (StringTerms) skuInfos.getAggregation(skuSpec);
            //将map封装为集合 流运算
            List<String> specList = specTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            resultMap.put("specList", formartSpec(specList));

            resultMap.put("pageNum",pageNum);
            return resultMap;
        }


        return null;
    }

    //规格数据格式转换   将list集合转换为map
    /**
     *[
     *"{}",
     * "{'颜色': '黑色', '尺码': '平光防蓝光-无度数电脑手机护目镜'}",
     *  "{'颜色': '红色', '尺码': '150度'}",
     *"{'颜色': '黑色', '尺码': '150度'}",
     *  "{'颜色': '黑色'}",
     * "{'颜色': '红色', '尺码': '100度'}",
     * "{'颜色': '红色', '尺码': '250度'}",
     *  "{'颜色': '红色', '尺码': '350度'}",
     * "{'颜色': '黑色', '尺码': '200度'}",
     *  "{'颜色': '黑色', '尺码': '250度'}"
     *     ]
     *
     *     颜色:[黑色,红色]
     *     尺码:[100,200,300]
     */
    public Map<String,Set<String>> formartSpec(List<String> specList){
        Map<String,Set<String>> formartMap = new HashMap<>();
            if (specList!=null&&specList.size()>0){
                for (String specJSON : specList) {
                    //将JSON转换为map
                    Map<String,String> specMap = JSONObject.parseObject(specJSON, Map.class);
                    //遍历map集合
                    for (String speckey : specMap.keySet()) {
                        //根据key获取 value
                        Set<String> valueList = formartMap.get(speckey);
                        if (valueList==null){
                            valueList=new HashSet<>();
                        }
                        //将规格信息存入set中
                        valueList.add(specMap.get(speckey));
                        formartMap.put(speckey,valueList);
                    }
                }
            }
        return formartMap;
    }

}
