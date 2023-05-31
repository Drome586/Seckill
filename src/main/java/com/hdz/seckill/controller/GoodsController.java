package com.hdz.seckill.controller;

import com.hdz.seckill.exception.GlobalException;
import com.hdz.seckill.pojo.User;
import com.hdz.seckill.service.IGoodsService;
import com.hdz.seckill.service.IUserService;
import com.hdz.seckill.vo.DetailVo;
import com.hdz.seckill.vo.GoodsVo;
import com.hdz.seckill.vo.RespBean;
import com.hdz.seckill.vo.RespBeanEnum;
import com.sun.deploy.nativesandbox.comm.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private IUserService userService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;
    @RequestMapping(value = "/toList",produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model,User user,
                         HttpServletRequest request,HttpServletResponse response){
//        if(StringUtils.isEmpty(ticket)){
//            return "login";
//        }
//
//        //User user = (User) session.getAttribute(ticket);
//        User user = userService.getUserByCookie(ticket,request,response);
//        if(null == user){
//            //return "login";
//            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
//        }

        //页面缓存
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList");
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        model.addAttribute("user",user);
        model.addAttribute("goodsList",goodsService.findGoodsVo());

        WebContext webContext = new WebContext(request,response,request.getServletContext(),request.getLocale()
        ,model.asMap());

        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", webContext);
        if(!StringUtils.isEmpty(html)){
            valueOperations.set("goodsList",html,60, TimeUnit.SECONDS);
        }
        return html;
    }

    @RequestMapping( "/detail/{goodsId}")
    @ResponseBody
    //之前是String
    public RespBean toDetail(@PathVariable Long goodsId, User user,
                             HttpServletRequest request,
                             HttpServletResponse response){
//        ValueOperations valueOperations = redisTemplate.opsForValue();
//        String html = (String) valueOperations.get("goodsDetail" + goodsId);
//
//        if(!StringUtils.isEmpty(html)){
//            return html;
//        }

//        model.addAttribute("user",user);
        GoodsVo goodsVo = goodsService.findGoodsByGoodsId(goodsId);
        Date start = goodsVo.getStartDate();
        Date end = goodsVo.getEndDate();
        Date nowDate = new Date();
        //秒杀状态码
        int seckillStatus = 0;
        //秒杀倒计时
        int remainSeconds = 0;
        //秒杀之前
        if(nowDate.before(start)){
            remainSeconds = (((int) ((start.getTime() - nowDate.getTime()) / 1000)));
        }else if(nowDate.after(end)){
            seckillStatus = 2;
            remainSeconds = -1;
        }else{
            seckillStatus = 0;
            remainSeconds = 0;
        }

//        model.addAttribute("remainSeconds",remainSeconds);
//        model.addAttribute("seckillStatus",seckillStatus);
//        model.addAttribute("goods",goodsVo);
        //URL缓存
//        WebContext Context = new WebContext(request,response,request.getServletContext(),request.getLocale()
//                ,model.asMap());
//        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", Context);
//
//        if(!StringUtils.isEmpty(html)){
//            valueOperations.set("goodsDetail" + goodsId,html,60,TimeUnit.SECONDS);
//        }

        DetailVo detailVo = new DetailVo();
        detailVo.setUser(user);
        detailVo.setGoodsVo(goodsVo);
        detailVo.setRemainStatus(remainSeconds);
        detailVo.setSeckillStatus(seckillStatus);
        return RespBean.success(detailVo);
    }
}
