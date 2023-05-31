package com.hdz.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hdz.seckill.pojo.User;
import com.hdz.seckill.vo.LoginVo;
import com.hdz.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hdz
 * @since 2022-12-24
 */
public interface IUserService extends IService<User> {

    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据redis中的cookie获取用户信息
     * @param userTicket
     * @return
     */


    User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response);

    /**
     *更新密码
     * @param userTicket
     * @param password
     * @return
     */
    RespBean updatePassword(String userTicket,String password,
                            HttpServletRequest request,
                            HttpServletResponse response);
}
