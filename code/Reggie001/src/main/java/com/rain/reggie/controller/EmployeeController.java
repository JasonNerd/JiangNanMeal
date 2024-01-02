package com.rain.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rain.reggie.common.R;
import com.rain.reggie.entity.Employee;
import com.rain.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * http://localhost:8080/backend/page/login/login.html
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee result = employeeService.getOne(queryWrapper);

        String pwd = employee.getPassword();
        pwd = DigestUtils.md5DigestAsHex(pwd.getBytes());
        if (result == null)
            return R.error("用户不存在");
        if (!result.getPassword().equals(pwd)) {
            return R.error("密码错误");
        }
        if (result.getStatus() == 0) {
            return R.error("账号已停用");
        }

        request.getSession().setAttribute("employee", result.getId());
        return R.success(result);
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

    /**
     * 新增员工
     */
    @PostMapping("")
    public R<String> insert(HttpServletRequest request, @RequestBody Employee employee){
        log.info("新增员工信息: {}", employee.toString());
        Long user_id = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateUser(user_id);
        employee.setCreateTime(LocalDateTime.now());
        employee.setCreateUser(user_id);
        employee.setUpdateTime(LocalDateTime.now());
        String pwd = DigestUtils.md5DigestAsHex("123456".getBytes());
        employee.setPassword(pwd);
        employeeService.save(employee);
        return R.success("添加新员工成功");
    }

    /**
     * 条件分页查询
     * http://localhost:8080/employee/page?page=1&pageSize=10&name=%E5%BC%A0
     */
    @GetMapping("/page")
    // 非rest风格
    public R<Page> query(int page, int pageSize, String name){
        log.info("page: {}, pageSize: {}, name: {}", page, pageSize, name);
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(!StringUtils.isEmpty(name), Employee::getName, name);
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        Page<Employee> pageInfo = new Page<>(page, pageSize);
        employeeService.page(pageInfo, queryWrapper);
        log.info("Page: {}", pageInfo);
        return R.success(pageInfo);
    }

    @GetMapping("/{empId}")
    public R<Employee> getById(@PathVariable Long empId){
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getId, empId);
        Employee emp = employeeService.getById(empId);
        return R.success(emp);
    }

    @PutMapping("")
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee){
        log.info("更新员工信息: {}", employee.toString());
        Long currentUser = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateUser(currentUser);
        employee.setUpdateTime(LocalDateTime.now());
        employeeService.updateById(employee);
        return R.success("更新成功");
    }
}
