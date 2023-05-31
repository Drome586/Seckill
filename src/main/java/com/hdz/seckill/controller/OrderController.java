package com.hdz.seckill.controller;


import com.hdz.seckill.pojo.User;
import com.hdz.seckill.service.IOrderService;
import com.hdz.seckill.vo.OrderDetailVo;
import com.hdz.seckill.vo.RespBean;
import com.hdz.seckill.vo.RespBeanEnum;
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
 * @since 2022-12-28
 */
@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private IOrderService iOrderService;
    @RequestMapping("/detail")
    @ResponseBody
    public RespBean detail(User user,Long orderId){
        if(user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        OrderDetailVo detail = iOrderService.detail(orderId);

        return RespBean.success(detail);


    }
}
