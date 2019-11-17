package com.changgou.entity;

/**
 * 常量类
 * @author zhang
 */
public class Constants {

    /**
     * 订单未评价
     */
    public static final String ORDER_ONT_RATE="0";
    /**
     * 订单已评价
     */
    public static final String ORDER_HAS_RATE="1";
    /**
     * 订单已发货
     */
    public static  final  String HAS_BEEN_SHIPPED="1";
    /**
     * 订单未发货
     */
    public static  final  String NOT_YET_SHIPPED="0";
    /**
     * 已送达
     */
    public static  final  String HAVE_BEEN_SENT="2";
    /**
     * 订单来源 web
     */
    public static  final  String SOURCE_OF_WEB_ORDER="1";
    /**
     * 订单来源 app
     */
    public static  final  String SOURCE_OF_APP_ORDER="2";
    /**
     * 订单来源 公众号
     */
    public static  final  String SOURCE_OF_PUBLIC_ORDER="3";

    /**
     * 订单来源 微信小程序
     */
    public static  final  String SOURCE_OF_SMALL_PROGRAM_ORDER="4";

    /**
     * 订单来源 H5手机页面
     */
    public static  final  String SOURCE_OF_H5_ORDER="5";
    /**
     * 订单状态 已完成
     */
    public static final String ORDER_FINISHED="1";
    /**
     * 订单状态 未完成
     */
    public static final String ORDER_UNFINISHED="0";
    /**
     * 已退货
     */
    public static final String ORDER_SALES_RETURN="2";
    /**
     * 未支付
     */
    public static final String ORDER_NOT_PAY="0";
    /**
     * 已支付
     */
    public static final String ORDER_PAID="1";
    /**
     * 支付失败
     */
    public static final String ORDER_PAY_FOR_FAILURE="2";

    /**
     * 是否退货 0 未退货
     */
    public static final String IS_NOT_RETURN="0";


}
