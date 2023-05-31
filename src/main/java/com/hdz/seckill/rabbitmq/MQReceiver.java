package com.hdz.seckill.rabbitmq;

import com.hdz.seckill.pojo.SeckillMessage;
import com.hdz.seckill.pojo.SeckillOrder;
import com.hdz.seckill.pojo.User;
import com.hdz.seckill.service.IGoodsService;
import com.hdz.seckill.service.IOrderService;
import com.hdz.seckill.utils.JsonUtil;
import com.hdz.seckill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MQReceiver {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IOrderService orderService;

    @RabbitListener(queues = "queue")
    public void reveive1(Object msg){
        log.info("接收消息"+msg);
    }

    @RabbitListener(queues = "seckillQueue")
    public void receive(String message){
        log.info("接收的消息：" + message);
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(message, SeckillMessage.class);
        Long goodsId = seckillMessage.getGoodsId();
        User user = seckillMessage.getUser();

        GoodsVo goodsVo = goodsService.findGoodsByGoodsId(goodsId);
        if(goodsVo.getStockCount() < 1){
            return;
        }
        //判断是否重复抢购
        SeckillOrder seckillOrder =
                (SeckillOrder)redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if(seckillOrder != null){
            return;
        }
        //下单操作
        orderService.seckill(user,goodsVo);
    }

}
