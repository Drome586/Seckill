package com.hdz.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hdz.seckill.pojo.Goods;
import com.hdz.seckill.vo.GoodsVo;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author hdz
 * @since 2022-12-28
 */
public interface GoodsMapper extends BaseMapper<Goods> {
    //获取商品列表
    //@Select("SELECT g.id,g.goods_name,g.goods_img,g.goods_detail,g.goods_price,g.goods_stock,sg.seckill_price,sg.stock_count,sg.start_date,sg.end_date FROM t_goods g LEFT JOIN t_seckill_goods AS sg ON g.id = sg.goods_id")
    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
