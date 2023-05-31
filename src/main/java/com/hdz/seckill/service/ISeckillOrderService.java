package com.hdz.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hdz.seckill.pojo.SeckillOrder;
import com.hdz.seckill.pojo.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hdz
 * @since 2022-12-28
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {

    /**
     * 获取秒杀返回结果
     * -1秒杀失败，0排队中
     * @param user
     * @param goodsId
     * @return
     */
    Long getResult(User user, Long goodsId);
}
