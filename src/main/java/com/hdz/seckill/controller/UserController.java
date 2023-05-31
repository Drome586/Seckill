package com.hdz.seckill.controller;


import com.hdz.seckill.rabbitmq.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author hdz
 * @since 2022-12-24
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private MQSender mqSender;

    /**
     * 测试发送mq消息
     */
    @RequestMapping("/mq")
    @ResponseBody
    public void send(){
        mqSender.send("hello");
    }
}
