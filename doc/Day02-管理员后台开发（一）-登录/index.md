---
title: "瑞吉外卖 - Day02-管理员后台开发（一）-登录"
date: 2024-01-02T16:23:24+08:00
draft: false
tags: ["Reggie", "SpringBoot", "JavaWeb"]
categories: ["Reggie"]
twemoji: true
lightgallery: true
---

登录功能开发, 主要是要校验登录账号及密码的准确性, 注意密码使用 base64 加密.
另外一个最重要的是要记住当前用户的id以记住登录状态, 并使用拦截器, 对于部分请求, 需要先校验登录状态, 若校验成功才放行.

### 1. 创建实体类
登录业务逻辑涉及到的是员工的登录与管理, 创建 Employee 实体类, 它包装 MySQL 查询返回的数据:

```java
package com.rain.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工实体
 */
@Data
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String username;
    private String name;
    private String password;
    private String phone;
    private String sex;
    private String idNumber;//身份证号码
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
```

### 2. 创建三层调用结构
#### mapper
创建 mapper 层, 它负责实际的数据库操作, 同时借助于 mybatisplus, 它的编写十分简洁:
```java
package com.rain.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rain.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee>{
}
```
接口先被声明为 Mapper 接口, 接着它继承了 mybatisplus 中的 BaseMapper, 同时指明泛型类型为 Employee.

#### service
创建 service 层, 该层负责接受 controller 的业务需求, 调用 Mapper 层完成业务逻辑开发.
对于 service 层, 需要设计接口和实现类:
```java
package com.rain.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rain.reggie.entity.Employee;

public interface EmployeeService extends IService<Employee> {
}
```
```java
package com.rain.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rain.reggie.entity.Employee;
import com.rain.reggie.mapper.EmployeeMapper;
import com.rain.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper,Employee> implements EmployeeService{
}
```
注意实现类先继承了 ServiceImpl, 指明泛型类型, 接着实现了 EmployeeService 接口.


#### controller
来到顶层的设计 controller 层, 该层负责接受前端发送的请求, 解析请求参数, 调用 service 层获得结果, 将结果封装后返回前端.

注意请求路径、请求方式、响应结果的形式、请求需要完成的功能等等在 API 文档中有明确规定.
这里需要创建一个通用的数据返回实体类, 包括数据内容/消息/响应码. 另外还包含一个临时字典.
```java
public class R<T>{
    private String msg;
    private Integer code;
    private T data;
    private Map map = new HashMap(); //动态数据

    public static <T> R<T> success(T object) {
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }

    public static <T> R<T> error(String msg) {
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }
}
```

### 3. 登录逻辑实现
员工登录
1. 获取密码并进行md5加密
2. 查询数据库校验是否存在该用户(用户名)
3. 不存在则直接返回失败信息
4. 否则继续校验密码
5. 不匹配则直接返回失败信息
6. 否则继续校验用户状态
7. 若为禁用状态则返回失败信息
8. 否则登陆成功, 返回成功信息, 同时向请求中写入登录信息, 也即员工ID.

注意 mybatisplus 的使用方法:
```java
package com.rain.reggie.controller;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    
    // http://localhost:8080/backend/page/login/login.html
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee query = employeeService.getOne(queryWrapper);

        String pwd = employee.getPassword();
        pwd = DigestUtils.md5DigestAsHex(pwd.getBytes())
        if (query == null)
            return R.error("用户不存在");
        if (!query.getPassword().equals(pwd))
            return R.error("密码错误");
        if (query.getStatus() == 0)
            return R.error("账户已停用");
        
        request.getSession().setAttribute("employee", result.getId());
        return R.success(query);
    }
    
    // 登出
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }
}
``` 

### 4. 过滤器/拦截器
为了实现未登录则无法访问后台资源的效果, 需要编写过滤器或者拦截器, 这里采用过滤器.
实现的关键点在于 `@WebFilter` 注解和重写 `doFilter()` 方法
先获取到请求路径, 检查路径是否需要拦截, 不需要拦截的例如登入登出请求以及静态资源访问请求等.
对于需要拦截的, 检查会话中是否存储 "employee" 键值对, 若检查到该键值对, 表示已登录
直接放行, 否则向响应中写入错误状态字符串.
```java
package com.rain.reggie.filter;

@Slf4j
@WebFilter(filterName = "loginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String uri = request.getRequestURI();
        log.info("拦截到请求{}", uri);

        if (ok(uri)){
            log.info("本次请求{}不需要处理", uri);
            filterChain.doFilter(request, response);
            return;
        }

        if (null != request.getSession().getAttribute("employee")){
            log.info("已登录");
            filterChain.doFilter(request, response);
            return;
        }
        log.info("未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    private static boolean ok(String uri){
        AntPathMatcher matcher = new AntPathMatcher();
        String[] ok_list = {
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"
        };
        for (String u: ok_list){
            if (matcher.match(u, uri))
                return true;
        }
        return false;
    }
}
```

注意为了使过滤器生效, 需要添加 Servlet 包扫描注解.
```java
package com.rain.reggie;

@SpringBootApplication
@ServletComponentScan
public class Reggie001Application {

    public static void main(String[] args) {
        SpringApplication.run(Reggie001Application.class, args);
    }

}
```
