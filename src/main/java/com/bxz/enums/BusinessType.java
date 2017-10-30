package com.bxz.enums;

/**
 * Created by baixiangzhu on 2017/10/26.
 */
public enum BusinessType {

    /**
     * 店铺首页
     */
    SHOP_INDEX,
    /**
     * 我的返利
     */
    MY_CASHBACK,
    /**
     * 返利列表
     */
    EVALUATE_LIST,
    /**
     * 追评页
     */
    ADDITIONAL_EVALUATE_PAGE,
    /**
     * 评价页
     */
    EVALUATE_PAGE,

    /**
     * 拒收订单数
     */
    REJECTION_ORDER,

    /**
     * 取消订单
     */
    CANCEL_ORDER,

    /**
     * 超时自动取消数
     */
    TIMEOUT_CANCEL_ORDER,

    /**
     * 退款订单数
     */
    REFUNDED_ORDER,

    /**
     * 总订单
     */
    TOTAL_ORDER;

    public static boolean isContains(String type){

        for(BusinessType item : BusinessType.values()){
            if(item.name().equals(type)){
                return true;
            }

        }

        return false;
    }
}
