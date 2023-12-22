package com.rain.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rain.reggie.common.R;
import com.rain.reggie.entity.Employee;
import com.rain.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * 1. 获取密码并进行md5加密
     * 2. 查询数据库校验是否存在该用户(用户名)
     * 3. 不存在则直接返回失败信息
     * 4. 否则继续校验密码
     * 5. 不匹配则直接返回失败信息
     * 6. 否则继续校验用户状态
     * 7. 若为禁用状态则返回失败信息
     * 8. 否则登陆成功, 返回成功信息, 同时向请求中写入登录信息, 也即员工ID.
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        String pwd = employee.getPassword();
        pwd = DigestUtils.md5DigestAsHex(pwd.getBytes());
        // 使用 QueryWrapper
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee result = employeeService.getOne(queryWrapper);
        // 校验
        if (result == null)
            return R.error("用户不存在");
        if (!result.getPassword().equals(pwd)) {
            return R.error("密码错误");
        }
        if (result.getStatus() == 0) {
            return R.error("账号已停用");
        }
        // 记录当前用户ID(登录状态)
        request.getSession().setAttribute("employee", result.getId());
        return R.success(result);
        // http://localhost:8080/backend/page/login/login.html
    }

    /**
     * 清除登录状态
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }
}
