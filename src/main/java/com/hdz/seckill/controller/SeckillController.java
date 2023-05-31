package com.hdz.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hdz.seckill.exception.GlobalException;
import com.hdz.seckill.pojo.Order;
import com.hdz.seckill.pojo.SeckillMessage;
import com.hdz.seckill.pojo.SeckillOrder;
import com.hdz.seckill.pojo.User;
import com.hdz.seckill.rabbitmq.MQSender;
import com.hdz.seckill.service.IGoodsService;
import com.hdz.seckill.service.IOrderService;
import com.hdz.seckill.service.ISeckillOrderService;
import com.hdz.seckill.utils.JsonUtil;
import com.hdz.seckill.vo.GoodsVo;
import com.hdz.seckill.vo.RespBean;
import com.hdz.seckill.vo.RespBeanEnum;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
@Slf4j
@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisScript<Long> redisScript;

    @Autowired
    private MQSender mqSender;
    //用来判断只要预减库存小于0时就屏蔽掉后面大量用户与redis的访问，内存标记；
    private Map<Long,Boolean> EmptyStock = new HashMap<>();

    //测试2版本
//    @RequestMapping("/doSeckill2")
//    public String doSeckill2(Model model, User user,Long goodsId){
//        if (user==null) {
//            return "login";
//        }
//        model.addAttribute("user",user);
//        GoodsVo goods = goodsService.findGoodsByGoodsId(goodsId);
//
//        //判断库存
//        if (goods.getStockCount() < 1) {
//            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
//            return "seckillFail";
//        }
//        //判断是否重复抢购
//        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId())
//                .eq("goods_id", goodsId));
//
//        if (seckillOrder!=null) {
//            model.addAttribute("errmsg",RespBeanEnum.REPEAT_ERROR.getMessage());
//            return "seckillFail";
//        }
//        Order order = orderService.seckill(user,goods);
//        model.addAttribute("order",order);
//        model.addAttribute("goods",goods);
//
//        return "orderDetail";
//    }
//

    @RequestMapping(value = "/{path}/doSeckill",method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(@PathVariable String path, User user, Long goodsId){
        if (user==null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
//        model.addAttribute("user",user);
//        GoodsVo goods = goodsService.findGoodsByGoodsId(goodsId);
        ValueOperations valueOperations = redisTemplate.opsForValue();

        boolean check = orderService.checkPath(user,path,goodsId);
        if(!check){
            return RespBean.error(RespBeanEnum.PATH_CHECK);
        }
        //判断库存
//        if (goods.getStockCount() < 1) {
//            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
//            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
//        }
        //判断是否重复抢购,下面可以通过redis来获取
//        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId())
//                .eq("goods_id", goodsId));
        SeckillOrder seckillOrder =
        (SeckillOrder)redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);

        if (seckillOrder!=null) {
            //model.addAttribute("errmsg",RespBeanEnum.REPEAT_ERROR.getMessage());
            return RespBean.error(RespBeanEnum.REPEAT_ERROR);
        }
        //通过内存标记减少redis的访问
        if(EmptyStock.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //预减库存
        Long stock = (Long) redisTemplate.execute(redisScript, Collections.singletonList("seckillGoods:" + goodsId),
                Collections.EMPTY_LIST);
        //Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        if(stock < 0){
            EmptyStock.put(goodsId,true);

            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //通过redis来获取。解决超买问题
//        SeckillOrder seckillOrder =
//                (SeckillOrder)redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
//
//        model.addAttribute("order",order);
//        model.addAttribute("goods",goods);

//        Order order = orderService.seckill(user,goods);
        SeckillMessage seckillMessage = new SeckillMessage(user,goodsId);
        mqSender.sendMessage(JsonUtil.object2JsonStr(seckillMessage));
        return RespBean.success(0);
    }

    //获取秒杀返回结果
    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user,Long goodsId){
        if(user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user,goodsId);

        return RespBean.success(orderId);
    }

    @RequestMapping(value = "/path",method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user,Long goodsId,String captcha){
        if(user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        //检验验证码是否失效
        boolean check = orderService.checkCaptch(user,goodsId,captcha);
        if(!check){
            return RespBean.error(RespBeanEnum.VERIFY_ERROR);
        }

        String path = orderService.getRealPath(user,goodsId);
        return RespBean.success(path);
    }

    @RequestMapping(value = "/captcha",method = RequestMethod.GET)
    public void verifyCode(User user, Long goodsId, HttpServletResponse response){
        if(user == null || goodsId<0){
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //设置请求头为输出图片的类型
        response.setContentType("image/jpg");
        response.setHeader("Pargam","No-cache");
        response.setHeader("Cache-Control","no-cache");
        response.setDateHeader("Expires",0);
        //生成验证码，将结果放入到redis
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);

        redisTemplate.opsForValue().set("captcha:" + user.getId()+":" + goodsId,captcha.text(),300,
                TimeUnit.SECONDS);

        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码失效",e.getMessage());
        }

    }



    /**
     * 将商品库存加载到redis
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if(CollectionUtils.isEmpty(list)){
            return;
        }

        list.forEach(goodsVo ->{
                redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(),goodsVo.getStockCount());
                //内存标记
                EmptyStock.put(goodsVo.getId(),false);
            }
        );
    }
}
