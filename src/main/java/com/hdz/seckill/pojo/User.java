package com.hdz.seckill.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author hdz
 * @since 2022-12-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id，手机号码
     */
    private Long id;

    private String nickname;

    /**
     * MD5(MD5(pass+固定salt))+salt
     */
    private String password;

    private String salt;

    /**
     * 头像
     */
    private String head;

    private Date registerData;

    private Date lastLoginData;

    /**
     * 登陆次数
     */
    private Integer loginCount;


}
