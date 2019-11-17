package com.changgou.goods.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.changgou.goods.dao.*;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.SpuService;
import com.changgou.util.IdWorker;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SpuServiceImpl implements SpuService{

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    /**
     * 查询全部列表
     *
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 根据ID查询商品
     *
     * @param id
     * @return
     */
    @Override
    public Goods findById(String id) {
        // return spuMapper.selectByPrimaryKey(id);
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //查询sku集合 根据条件查询 条件-->id = "spuId"
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", id);
        List<Sku> skus = skuMapper.selectByExample(example);
        //封装 返回
        Goods goods = new Goods();
        goods.setSpu(spu);
        goods.setSkuList(skus);
        return goods;
    }

    @Override
    public Spu findSpuById(String id) {
        return  spuMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     *
     * @param goods
     */
    @Autowired
    private IdWorker idWorker;

    @Transactional
    @Override
    public void add(Goods goods) {
        // spuMapper.insert(goods);
        //注入idwoker
        //添加spu   设置分布式id
        Spu spu = goods.getSpu();
        long id = idWorker.nextId();
        spu.setId(String.valueOf(id));
        //设置当前删除状态
        spu.setIsDelete("0");
        //设置当前上架状态
        spu.setIsMarketable("0");
        //设置当前审核状态
        spu.setStatus("0");
        spuMapper.insertSelective(spu);
        //保存sku集合数据到数据库
        saveSkuList(goods);

    }

    //添加sku集合
    public void saveSkuList(Goods goods) {
        //获取spu对象

        Spu spu = goods.getSpu();
        //创建日期
        Date date = new Date();
        //创建category对象
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
        //创建 brand对象
        Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
        /**
         * 添加分类与品牌之间的关联
         */
        CategoryBrand categoryBrand = new CategoryBrand();
        categoryBrand.setBrandId(spu.getBrandId());
        categoryBrand.setCategoryId(spu.getCategory3Id());
        int count = categoryBrandMapper.selectCount(categoryBrand);
        //如果没有关系添加关系
        if (count == 0) {
            categoryBrandMapper.insert(categoryBrand);
        }

        //获取sku集合对象
        List<Sku> skuList = goods.getSkuList();


        //设置主键
        if (skuList != null) {

            for (Sku sku : skuList) {

                long a = idWorker.nextId();
                sku.setId(String.valueOf(a));
                //设置规格
                if (sku.getSpec() == null || "".equals(sku.getSpec())) {
                    sku.setSpec("{}");
                }
                //设置skuList名称==  商品名称+规格
                String name = spu.getName();
                //将json转换为map
                Map<String, String> specMap = JSONObject.parseObject(sku.getSpec(), Map.class);

                if (specMap != null && specMap.size() > 0) {
                    for (String value : specMap.values()) {
                        name += " " + value;
                    }
                }
                //设置名称
                sku.setName(name);
                //设置spu的ID
                sku.setSpuId(spu.getId());
                //设置创建日期
                sku.setCreateTime(date);
                //设置修改日期
                sku.setUpdateTime(date);
                //商品分类ID
                sku.setCategoryId(category.getId());
                //商品分类名称
                sku.setCategoryName(category.getName());
                //品牌名称
                sku.setBrandName(brand.getName());
                //添加数据
                skuMapper.insertSelective(sku);


            }


        }

    }


    /**
     * 修改
     *
     * @param goods
     */
    @Override
    @Transactional
    public void update(Goods goods) {
        // spuMapper.updateByPrimaryKey(spu);
        //修改spu
        Spu spu = goods.getSpu();
        spuMapper.updateByPrimaryKey(spu);

        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", spu.getId());
        //修改sku
        List<Sku> skuList = goods.getSkuList();
        for (Sku sku : skuList) {
            skuMapper.updateByExample(sku, example);
        }
      /*  skuMapper.deleteByExample(example);
        saveSkuList(goods);*/

    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(String id) {
        //判断当前商品是否处于下架状态
        Spu spu = spuMapper.selectByPrimaryKey(id);
        String marketable = spu.getIsMarketable();
        //如果是下架状态,修改相对应的标记为 逻辑删除
        if ("1".equals(marketable)) {
            throw new RuntimeException("该商品还未下架,不能删除");
        }
        //删除
        spu.setIsDelete("1");
        //设置为未审核状态
        spu.setStatus("0");
        spuMapper.updateByPrimaryKey(spu);
    }


    /**
     * 条件查询
     *
     * @param searchMap
     * @return
     */
    @Override
    public List<Spu> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Spu> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        return (Page<Spu>) spuMapper.selectAll();
    }

    /**
     * 条件+分页查询
     *
     * @param searchMap 查询条件
     * @param page      页码
     * @param size      页大小
     * @return 分页结果
     */
    @Override
    public Page<Spu> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        return (Page<Spu>) spuMapper.selectByExample(example);
    }

    //商品审核并自动上架
    @Transactional
    @Override
    public void audit(String id) {
        //校验商品是否被删除
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("商品不存在");
        }
        String s = spu.getIsDelete();
        //商品处于删除状态
        if ("1".equals(s)) {
            throw new RuntimeException("商品处于删除状态");
        }
        //不处于删除状态 将审核状态修改为1 上架状态为1
        spu.setStatus("1");
        spu.setIsMarketable("1");
        //修改操作
        spuMapper.updateByPrimaryKey(spu);
    }

    //下架商品
    @Override
    public void pull(String id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //判断当前商品是否处于删除状态
        String isDelete = spu.getIsDelete();
        if ("1".equals(isDelete)) {
            throw new RuntimeException("该商品已被删除");
        }
        spu.setIsMarketable("0");
        spuMapper.selectByPrimaryKey(spu);
    }

    //商品上架
    @Override
    public void put(String id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("该商品不存在");
        }
        //判断当前商品是否已审核
        String status = spu.getStatus();
        if (!"1".equals(status)) {

            throw new RuntimeException("该商品还未审核,不能上架");
        }
        //商品审核 将商品改为上架状态
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKey(spu);

    }

    //还原被删除的商品
    @Override
    @Transactional
    public void restore(String id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("该商品已不存在");
        }

        //判断当前的商品必须处于删除的状态
        String isDelete = spu.getIsDelete();
        if (!"1".equals(isDelete)) {
            throw new RuntimeException("该商品未被删除");
        }else {
            //修改删除状态
            spu.setIsDelete("0");
            //审核状态
            spu.setStatus("0");
            spuMapper.updateByPrimaryKey(spu);
        }



    }
    //物理删除
    @Override
    public void deletes(String id) {
       //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu==null){
            throw  new RuntimeException("该商品不存在");
        }
        //判断当前商品是否处于删除状态
        String isDelete = spu.getIsDelete();
        if (!"1".equals(isDelete)){
            throw new RuntimeException("该商品未进行物理删除 请物理删除后做此操作");
        }
        //执行删除操作
        spuMapper.deleteByPrimaryKey(id);
    }

    /**
     * 构建查询对象
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 主键
            if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                criteria.andEqualTo("id", searchMap.get("id"));
            }
            // 货号
            if (searchMap.get("sn") != null && !"".equals(searchMap.get("sn"))) {
                criteria.andEqualTo("sn", searchMap.get("sn"));
            }
            // SPU名
            if (searchMap.get("name") != null && !"".equals(searchMap.get("name"))) {
                criteria.andLike("name", "%" + searchMap.get("name") + "%");
            }
            // 副标题
            if (searchMap.get("caption") != null && !"".equals(searchMap.get("caption"))) {
                criteria.andLike("caption", "%" + searchMap.get("caption") + "%");
            }
            // 图片
            if (searchMap.get("image") != null && !"".equals(searchMap.get("image"))) {
                criteria.andLike("image", "%" + searchMap.get("image") + "%");
            }
            // 图片列表
            if (searchMap.get("images") != null && !"".equals(searchMap.get("images"))) {
                criteria.andLike("images", "%" + searchMap.get("images") + "%");
            }
            // 售后服务
            if (searchMap.get("saleService") != null && !"".equals(searchMap.get("saleService"))) {
                criteria.andLike("saleService", "%" + searchMap.get("saleService") + "%");
            }
            // 介绍
            if (searchMap.get("introduction") != null && !"".equals(searchMap.get("introduction"))) {
                criteria.andLike("introduction", "%" + searchMap.get("introduction") + "%");
            }
            // 规格列表
            if (searchMap.get("specItems") != null && !"".equals(searchMap.get("specItems"))) {
                criteria.andLike("specItems", "%" + searchMap.get("specItems") + "%");
            }
            // 参数列表
            if (searchMap.get("paraItems") != null && !"".equals(searchMap.get("paraItems"))) {
                criteria.andLike("paraItems", "%" + searchMap.get("paraItems") + "%");
            }
            // 是否上架
            if (searchMap.get("isMarketable") != null && !"".equals(searchMap.get("isMarketable"))) {
                criteria.andEqualTo("isMarketable", searchMap.get("isMarketable"));
            }
            // 是否启用规格
            if (searchMap.get("isEnableSpec") != null && !"".equals(searchMap.get("isEnableSpec"))) {
                criteria.andEqualTo("isEnableSpec", searchMap.get("isEnableSpec"));
            }
            // 是否删除
            if (searchMap.get("isDelete") != null && !"".equals(searchMap.get("isDelete"))) {
                criteria.andEqualTo("isDelete", searchMap.get("isDelete"));
            }
            // 审核状态
            if (searchMap.get("status") != null && !"".equals(searchMap.get("status"))) {
                criteria.andEqualTo("status", searchMap.get("status"));
            }

            // 品牌ID
            if (searchMap.get("brandId") != null) {
                criteria.andEqualTo("brandId", searchMap.get("brandId"));
            }
            // 一级分类
            if (searchMap.get("category1Id") != null) {
                criteria.andEqualTo("category1Id", searchMap.get("category1Id"));
            }
            // 二级分类
            if (searchMap.get("category2Id") != null) {
                criteria.andEqualTo("category2Id", searchMap.get("category2Id"));
            }
            // 三级分类
            if (searchMap.get("category3Id") != null) {
                criteria.andEqualTo("category3Id", searchMap.get("category3Id"));
            }
            // 模板ID
            if (searchMap.get("templateId") != null) {
                criteria.andEqualTo("templateId", searchMap.get("templateId"));
            }
            // 运费模板id
            if (searchMap.get("freightId") != null) {
                criteria.andEqualTo("freightId", searchMap.get("freightId"));
            }
            // 销量
            if (searchMap.get("saleNum") != null) {
                criteria.andEqualTo("saleNum", searchMap.get("saleNum"));
            }
            // 评论数
            if (searchMap.get("commentNum") != null) {
                criteria.andEqualTo("commentNum", searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
