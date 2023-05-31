package com.hdz.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import sun.management.snmp.jvmmib.EnumJvmMemPoolType;

@Getter
@ToString
@AllArgsConstructor
public enum RespBeanEnum {

    //状态码
    SUCCESS(200,"SUCCESS"),
    ERROR(500,"服务端异常"),
    //登录码
    LOGIN_ERROR(500201,"用户名或密码错误"),
    MOBILE_ERROR(500202,"手机号格式错误"),
    BIND_ERROR(500203, "参数校验异常"),
    MOBILE_NOT_EXIST(500204,"手机号不存在"),
    UPDATE_FAIL(500205,"更新密码失败"),
    //秒杀
    SESSION_ERROR(500206,"会话错误"),
    EMPTY_STOCK(500301,"秒杀库存不足"),
    REPEAT_ERROR(500302,"重复抢购"),
    //订单
    ORDER_NOT_EXIST(500400,"订单不存在"),
    REQUEST_ILLEGAL(500401,"验证码请求时用户不存在"),
    VERIFY_ERROR(500402,"验证码错误"),
    PATH_CHECK(500401,"路径错误");

    private final Integer code;
    private final String message;
}
