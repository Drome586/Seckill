package com.hdz.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hdz.seckill.exception.GlobalException;
import com.hdz.seckill.mapper.OrderMapper;
import com.hdz.seckill.pojo.Order;
import com.hdz.seckill.pojo.SeckillGoods;
import com.hdz.seckill.pojo.SeckillOrder;
import com.hdz.seckill.pojo.User;
import com.hdz.seckill.service.IGoodsService;
import com.hdz.seckill.service.IOrderService;
import com.hdz.seckill.service.ISeckillGoodsService;
import com.hdz.seckill.service.ISeckillOrderService;
import com.hdz.seckill.utils.MD5;
import com.hdz.seckill.utils.UUIDUtil;
import com.hdz.seckill.vo.GoodsVo;
import com.hdz.seckill.vo.OrderDetailVo;
import com.hdz.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hdz
 * @since 2022-12-28
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    @Autowired
    private ISeckillGoodsService seckillGoodsService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;
    @Transactional
    @Override
    public Order seckill(User user, GoodsVo goods) {
        ValueOperations valueOperations = redisTemplate.opsForValue();

        //秒杀商品减掉库存
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id",
                goods.getId()));
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);

        //seckillGoodsService.updateById(seckillGoods);
        //判断库存，确保库存大于0然后才能做减的操作
        boolean updateResult = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().setSql("stock_count = "+
                "stock_count-1").eq("goods_id", seckillGoods.getId()).gt("stock_count", 0));

        if(seckillGoods.getStockCount() < 1){
            valueOperations.set("isStockEmpty:" + goods.getId(),"0");
            return null;
        }
        Order order = new Order();
        //生成订单
        order.setUserId(user.getId());
        order.setGoodsId(goods.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);

        //生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(goods.getId());
        seckillOrderService.save(seckillOrder);
        //这样就可以通过redis来获取
        redisTemplate.opsForValue().set("order:"+user.getId() + ":" + goods.getId(),seckillOrder);
        return order;
    }

    @Override
    public OrderDetailVo detail(Long orderId) {
        if(orderId == null){
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsByGoodsId(order.getGoodsId());

        OrderDetailVo detail = new OrderDetailVo();

        detail.setOrder(order);
        detail.setGoodsVo(goodsVo);
        return detail;
    }

    @Override
    public String getRealPath(User user,Long goodsId) {
        String str = MD5.md5(UUIDUtil.uuid() + "123456");
        redisTemplate.opsForValue().set("seckillPath:"+user.getId()+":" + goodsId,
                str,60, TimeUnit.SECONDS);
        return str;
    }

    @Override
    public boolean checkPath(User user, String path, Long goodsId) {
        if(user == null || goodsId < 0 || StringUtils.isEmpty(path)){
            return false;
        }
        String redisPath = (String) redisTemplate.opsForValue().get("seckillPath:" + user.getId() + ":" + goodsId);

        return path.equals(redisPath);
    }

    @Override
    public boolean checkCaptch(User user, Long goodsId, String captcha) {
        if(StringUtils.isEmpty(captcha) || user == null || goodsId < 0){
            return false;
        }
        String redisCaptcha = (String) redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);

        return captcha.equals(redisCaptcha);
    }
}
