package com.rain.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rain.reggie.common.R;
import com.rain.reggie.entity.User;
import com.rain.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("user")
@Slf4j
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping("login")
    public R<User> login(HttpServletRequest request, @RequestBody User user){
        log.info("登录用户: {}", user);
        String phone = user.getPhone();
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getPhone, phone);
        User query = userService.getOne(userWrapper);
        if (query == null){
            log.info("新用户, 自动进行注册.");
            userService.save(user);
            request.getSession().setAttribute("user", user.getId());
        }else
            request.getSession().setAttribute("user", query.getId());
        return R.success(user);
    }

    @PostMapping("loginout")
    public R<String> logout(HttpServletRequest request){
        log.info("用户{}退出登录", request.getSession().getAttribute("user"));
        request.getSession().removeAttribute("user");
        return R.success("退出成功");
    }
}
