package com.hdz.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hdz.seckill.pojo.Order;
import com.hdz.seckill.pojo.User;
import com.hdz.seckill.vo.GoodsVo;
import com.hdz.seckill.vo.OrderDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hdz
 * @since 2022-12-28
 */
public interface IOrderService extends IService<Order> {

    Order seckill(User user, GoodsVo goods);

    OrderDetailVo detail(Long orderId);

    String getRealPath(User user, Long goodsId);

    boolean checkPath(User user, String path, Long goodsId);

    boolean checkCaptch(User user, Long goodsId, String captcha);
}
