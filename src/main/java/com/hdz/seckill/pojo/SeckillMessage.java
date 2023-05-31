package com.hdz.seckill.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//消息对象
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillMessage {
    private User user;

    private Long goodsId;
}
