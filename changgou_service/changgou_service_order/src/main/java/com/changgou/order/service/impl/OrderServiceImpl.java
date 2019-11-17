package com.changgou.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fescar.spring.annotation.GlobalTransactional;
import com.changgou.entity.Constants;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.order.config.RabbitMQConfig;
import com.changgou.order.dao.*;
import com.changgou.order.pojo.*;
import com.changgou.order.service.CartService;
import com.changgou.order.service.OrderService;
import com.changgou.pay.feign.WxPayFeign;
import com.changgou.util.IdWorker;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 10173
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private CartService cartService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private OrderLogMapper orderLogMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private WxPayFeign wxPayFeign;
    @Autowired
    private OrderConfigMapper orderConfigMapper;

    /**
     * 查询全部列表
     *
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    @Override
    public Order findById(String id) {
        return orderMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     *
     * @param order
     */

    @GlobalTransactional(name = "order_add")
    public String add(Order order) {
        //获取购物车的相关数据
        Map map = cartService.list(order.getUsername());
        List<OrderItem> orderItemList = (List<OrderItem>) map.get("orderItemList");
        //统计计算 总金额 总数量
        //填充订单数据并保存到tb_order中
        order.setId(idWorker.nextId() + "");
        order.setTotalNum((Integer) map.get("totalNum"));
        order.setTotalMoney((Integer) map.get("totalPrice"));
        order.setPayMoney((Integer) map.get("totalPrice"));
        order.setCreateTime(new Date());
        order.setUpdateTime(order.getCreateTime());
        order.setBuyerRate(Constants.ORDER_ONT_RATE); //商品未评价
        order.setSourceType(Constants.SOURCE_OF_WEB_ORDER); //来源web
        order.setOrderStatus(Constants.ORDER_UNFINISHED); //未完成
        order.setPayStatus(Constants.ORDER_NOT_PAY);  //未支付
        order.setConsignStatus(Constants.NOT_YET_SHIPPED);  //未发货
        orderMapper.insertSelective(order);
        //填充订单数据并保存到tb_order_item中
        for (OrderItem orderItem : orderItemList) {
            orderItem.setId(idWorker.nextId() + "");
            orderItem.setIsReturn(Constants.IS_NOT_RETURN);
            orderItem.setOrderId(order.getId());
            orderItemMapper.insertSelective(orderItem);
        }

        //扣减库存并增加销量
        skuFeign.decrCount(order.getUsername());

        //向订单数据库的任务表中添加数据
        Task task = new Task();
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        task.setMqExchange(RabbitMQConfig.EX_BUYING_ADDPOINTUSER);
        task.setMqRoutingkey(RabbitMQConfig.CG_BUYING_ADDPOINT_KEY);

        Map map1 = new HashMap();
        map1.put("username", order.getUsername());
        map1.put("orderId", order.getId());
        map1.put("point", order.getPayMoney());
        task.setRequestBody(JSON.toJSONString(map1));
        taskMapper.insertSelective(task);

        //清除redis中缓存购物车数据
        redisTemplate.delete("Cart" + order.getUsername());

        rabbitTemplate.convertAndSend("", "queue.ordercreate", order.getId());
        //返回订单id
        return order.getId();

    }


    /**
     * 修改
     *
     * @param order
     */
    @Override
    public void update(Order order) {
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey(id);
    }


    /**
     * 条件查询
     *
     * @param searchMap
     * @return
     */
    @Override
    public List<Order> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return orderMapper.selectByExample(example);
    }

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Order> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        return (Page<Order>) orderMapper.selectAll();
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
    public Page<Order> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        return (Page<Order>) orderMapper.selectByExample(example);
    }

    //修改订单状态为已支付
    @Override
    public void updatePayStatus(String orderId, String transactionId) {
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(orderId);
        //存在订单且状态为0
        if (order != null && "0".equals(order.getPayStatus())) {
            //修改订单日志
            order.setPayStatus("1");
            order.setOrderStatus("1");
            order.setUpdateTime(new Date());
            order.setPayTime(new Date());
            order.setTransactionId(transactionId);//微信返回的交易流水号
            orderMapper.updateByPrimaryKeySelective(order);
            //记录订单日志
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId() + "");
            orderLog.setOperater("system");// 操作员 系统
            orderLog.setOperateTime(new Date());//当前日期
            orderLog.setOrderStatus("1");
            orderLog.setPayStatus("1");
            orderLog.setRemarks("支付流水号" + transactionId);
            orderLog.setOrderId(order.getId());
            orderLogMapper.insert(orderLog);

        }


    }


    //关闭订单操作
    @Override
    @Transactional
    public void closeOrder(String orderId) {
        System.out.println("关闭订单业务开启" + orderId);
        //1.根据订单id查询mysql订单信息 判断订单是否存在 判断订单的支付状态
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"0".equals(order.getOrderStatus())) {
            System.out.println("此订单已支付不需要关闭");
            return;
        }
        System.out.println("关闭订单通过校验" + orderId);
        //调用微信订单查询 检测支付状态
        Map wxQueryMap = (Map) wxPayFeign.queryOrder(orderId).getData();
        System.out.println("查询微信支付订单: "+wxQueryMap);
        //如果支付状态是成功 进行补偿
        if ("SUCCESS".equals(wxQueryMap.get("trade_state"))){
            updatePayStatus(orderId,(String)wxQueryMap.get("transaction_id"));
            System.out.println("补偿");
        }
        //如果是未支付状态关闭订单   修改mysql中的订单信息 新增订单日志,恢复商品的库存
        if ("NOTPAY".equals(wxQueryMap.get("trade_state"))){
            System.out.println("订单未支付 关闭订单");
            order.setCloseTime(new Date());//关闭时间
            order.setOrderStatus("4"); //关闭状态
            orderMapper.updateByPrimaryKeySelective(order);
            //记录订单变动日志
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId()+"");
            orderLog.setOperater("system"); //操作员系统
            orderLog.setOperateTime(new Date());//当前日期
            orderLog.setOrderStatus("4");
            orderLog.setOrderId(order.getId());
            orderLogMapper.insert(orderLog);

            //恢复库存和销量
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            List<OrderItem> itemList = orderItemMapper.select(orderItem);
            for (OrderItem item : itemList) {
                //回滚库存
                skuFeign.resumeStockNum(item.getSkuId(),item.getNum());
            }
            //关闭微信订单
            wxPayFeign.closeOrder(orderId);
        }
    }
    //批量发货
    @Override
    @Transactional
    public void batchSend(List<Order> orders) {
        //判断每一个订单的运单号和物流公司的值是否存在
        for (Order order : orders) {
            if (order.getId()==null){
                throw  new RuntimeException("订单号不存在");

            }
            if (order.getShippingCode()==null||order.getShippingName()==null){
                throw  new RuntimeException("快递单号和快递公司不能为空");
            }
        }
        //循环订单,进行订单状态的校验
        for (Order order : orders) {
            Order order1 = orderMapper.selectByPrimaryKey(order.getId());

            if (!"0".equals(order1.getConsignStatus())||!"1".equals(order1.getOrderStatus())){
                throw new RuntimeException("订单状态有误");
            }
        }
        //循环订单更新操作
        for (Order order : orders) {
            order.setOrderStatus("2");// 订单状态已发货
            order.setConsignStatus("1");// 发货状态 已发货
            order.setConsignTime(new Date());//发货时间
            order.setUpdateTime( new Date());//更新时间
            orderMapper.updateByPrimaryKeySelective(order);
            //记录订单变动日志
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId()+"");
            orderLog.setOperateTime(new Date());//当前日期
            orderLog.setOperater( "admin" );//系统管理员
            orderLog.setOrderStatus("2"); //已完成
            orderLog.setConsignStatus( "1" );//发状态（0未发货 1已发货）
            orderLog.setOrderId(order.getId());
            orderLogMapper.insertSelective(orderLog);
        }
    }

    /**
     * 手动确认收货
     * @param orderId
     * @param operator
     */
    @Override
    @Transactional
    public void confirmTask(String orderId, String operator) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order==null){
            throw  new RuntimeException("订单不存在");
        }
        if (!"1".equals(order.getConsignStatus())){
            throw new RuntimeException("订单未发货");
        }
        order.setConsignStatus("2"); //配送状态 已送达
        order.setOrderStatus("3"); //订单状态 已完成
        order.setUpdateTime(new Date());
        order.setEndTime( new Date() );//交易结束
        //记录订单日志变动
        OrderLog orderLog = new OrderLog();
        orderLog.setId( idWorker.nextId()+"" );
        orderLog.setOperateTime(new Date());//当前日期
        orderLog.setOperater( operator );//操作员
        orderLog.setOrderStatus("3");  //订单状态
        orderLog.setOrderId(order.getId()); //订单号
        orderLogMapper.insertSelective(orderLog);

    }
    //自动收货
    @Override
    @Transactional
    public void autoTack() {
        //1.从订单的配置表中获取订单自动确认的时点
        OrderConfig orderConfig = orderConfigMapper.selectByPrimaryKey("1");
        //2.得到当前的时间节点向前数(订单自动确认的时间节点)天,作为过期的时间节点
        LocalDate date =LocalDate.now();
        LocalDate days = date.plusDays(-orderConfig.getTakeTimeout());
        System.out.println(days);
        //3.从订单表中获取相关符合条件的数据(发货时间小于过期时间,收货状态为未确认)
                //按条件查询 获取订单列表
            Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();

        criteria.andLessThan("consignTime",days);
        criteria.andEqualTo("orderStatus","2");
            //查询获取订单集合
        List<Order> orders = orderMapper.selectByExample(example);
        //循环遍历 执行确认收货
        for (Order order : orders) {
            System.out.println("过期订单: " +order.getId()+""+order.getConsignStatus());
            confirmTask(order.getId(),"system");
        }
    }

    /**
     * 构建查询对象
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 订单id
            if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                criteria.andEqualTo("id", searchMap.get("id"));
            }
            // 支付类型，1、在线支付、0 货到付款
            if (searchMap.get("payType") != null && !"".equals(searchMap.get("payType"))) {
                criteria.andEqualTo("payType", searchMap.get("payType"));
            }
            // 物流名称
            if (searchMap.get("shippingName") != null && !"".equals(searchMap.get("shippingName"))) {
                criteria.andLike("shippingName", "%" + searchMap.get("shippingName") + "%");
            }
            // 物流单号
            if (searchMap.get("shippingCode") != null && !"".equals(searchMap.get("shippingCode"))) {
                criteria.andLike("shippingCode", "%" + searchMap.get("shippingCode") + "%");
            }
            // 用户名称
            if (searchMap.get("username") != null && !"".equals(searchMap.get("username"))) {
                criteria.andLike("username", "%" + searchMap.get("username") + "%");
            }
            // 买家留言
            if (searchMap.get("buyerMessage") != null && !"".equals(searchMap.get("buyerMessage"))) {
                criteria.andLike("buyerMessage", "%" + searchMap.get("buyerMessage") + "%");
            }
            // 是否评价
            if (searchMap.get("buyerRate") != null && !"".equals(searchMap.get("buyerRate"))) {
                criteria.andLike("buyerRate", "%" + searchMap.get("buyerRate") + "%");
            }
            // 收货人
            if (searchMap.get("receiverContact") != null && !"".equals(searchMap.get("receiverContact"))) {
                criteria.andLike("receiverContact", "%" + searchMap.get("receiverContact") + "%");
            }
            // 收货人手机
            if (searchMap.get("receiverMobile") != null && !"".equals(searchMap.get("receiverMobile"))) {
                criteria.andLike("receiverMobile", "%" + searchMap.get("receiverMobile") + "%");
            }
            // 收货人地址
            if (searchMap.get("receiverAddress") != null && !"".equals(searchMap.get("receiverAddress"))) {
                criteria.andLike("receiverAddress", "%" + searchMap.get("receiverAddress") + "%");
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if (searchMap.get("sourceType") != null && !"".equals(searchMap.get("sourceType"))) {
                criteria.andEqualTo("sourceType", searchMap.get("sourceType"));
            }
            // 交易流水号
            if (searchMap.get("transactionId") != null && !"".equals(searchMap.get("transactionId"))) {
                criteria.andLike("transactionId", "%" + searchMap.get("transactionId") + "%");
            }
            // 订单状态
            if (searchMap.get("orderStatus") != null && !"".equals(searchMap.get("orderStatus"))) {
                criteria.andEqualTo("orderStatus", searchMap.get("orderStatus"));
            }
            // 支付状态
            if (searchMap.get("payStatus") != null && !"".equals(searchMap.get("payStatus"))) {
                criteria.andEqualTo("payStatus", searchMap.get("payStatus"));
            }
            // 发货状态
            if (searchMap.get("consignStatus") != null && !"".equals(searchMap.get("consignStatus"))) {
                criteria.andEqualTo("consignStatus", searchMap.get("consignStatus"));
            }
            // 是否删除
            if (searchMap.get("isDelete") != null && !"".equals(searchMap.get("isDelete"))) {
                criteria.andEqualTo("isDelete", searchMap.get("isDelete"));
            }

            // 数量合计
            if (searchMap.get("totalNum") != null) {
                criteria.andEqualTo("totalNum", searchMap.get("totalNum"));
            }
            // 金额合计
            if (searchMap.get("totalMoney") != null) {
                criteria.andEqualTo("totalMoney", searchMap.get("totalMoney"));
            }
            // 优惠金额
            if (searchMap.get("preMoney") != null) {
                criteria.andEqualTo("preMoney", searchMap.get("preMoney"));
            }
            // 邮费
            if (searchMap.get("postFee") != null) {
                criteria.andEqualTo("postFee", searchMap.get("postFee"));
            }
            // 实付金额
            if (searchMap.get("payMoney") != null) {
                criteria.andEqualTo("payMoney", searchMap.get("payMoney"));
            }

        }
        return example;
    }

}
