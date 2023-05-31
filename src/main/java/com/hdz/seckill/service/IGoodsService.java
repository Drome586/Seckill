package com.hdz.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hdz.seckill.pojo.Goods;
import com.hdz.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hdz
 * @since 2022-12-28
 */
public interface IGoodsService extends IService<Goods> {

    //返回查询的商品列表
    List<GoodsVo> findGoodsVo();
    //显示商品详情
    GoodsVo findGoodsByGoodsId(Long goodsId);
}
