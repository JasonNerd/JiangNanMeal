---
title: "Day04-Reggie-公共字段填充与菜品分类管理"
date: 2024-01-04T14:34:57+08:00
draft: false
tags: ["Reggie", "SpringBoot", "JavaWeb"]
categories: ["Reggie"]
twemoji: true
lightgallery: true
---

### 1. 公共字段填充
前面我们已经完成了后台系统的员工管理功能开发，在新增员工时需要设置创建时间、创建人、修改时间、修改人等字段，在编辑员工时需要设置修改时间和修改人等字段。这些字段属于公共字段，也就是很多表中都有这些字段. 因此可以设置一个自动填充的功能类.

基本步骤是:
1. 为公共自动填充字段添加 `@TableField` 注解, 如下(`Employee`), 注意指明填充条件`fill`:
   ```java
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
    ```
2. 编写 FFMetaObjHandler 类, 它交给 Bean 容器进行管理, 同时实现 `MetaObjectHandler`接口, 重写 `insertFill` 方法和 `updateFill` 方法.
   核心的方法是 setValue, 设置字段名对应的值
    ```java
    package com.rain.reggie.common;
    @Component
    @Slf4j
    public class FFMetaObjHandler implements MetaObjectHandler {
        /**
        * 公共字段填充
        */
        @Override
        public void insertFill(MetaObject metaObject) {
            log.info("公共字段填充-[insert]: {}", metaObject.toString());
            metaObject.setValue("createTime", LocalDateTime.now());
            metaObject.setValue("updateTime", LocalDateTime.now());
            metaObject.setValue("createUser", BaseContext.getId());
            metaObject.setValue("updateUser", BaseContext.getId());
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            log.info("公共字段填充-[update]: {}", metaObject.toString());
            metaObject.setValue("updateTime", LocalDateTime.now());
            metaObject.setValue("updateUser", BaseContext.getId());
        }
    }
    ```
3. 去掉 controller 层 insert update 方法中关于更新 这几个字段的操作:
   注意参数 `HttpServletRequest request` 也被删去了.
   ```java
    @PutMapping
    public R<String> update(@RequestBody Employee employee){
        log.info("更新员工信息: {}", employee.toString());
        employeeService.updateById(employee);
        return R.success("更新成功");
    }
    @PostMapping
    public R<String> insert(@RequestBody Employee employee){
        log.info("新增员工信息: {}", employee.toString());
        String pwd = DigestUtils.md5DigestAsHex("123456".getBytes());
        employee.setPassword(pwd);
        employeeService.save(employee);
        return R.success("添加新员工成功");
    }
   ```

需要注意:
操作用户是当前登录的用户, 而为了获取当前登录用户的ID, 通常做法是通过 request 的 session 存储进行访问:
```java
Long user_id = (Long) request.getSession().getAttribute("employee");
```
然而, `FFMetaObjHandler` 类并不在三层结构之内, 因此无法获取到 request 对象. 一种可行的解决方法是使用 ThreadLocal 变量.
ThreadLocal 变量的作用是保存线程的一个局部变量, 对于同一个线程其存取的变量是一致的, 可用于线程内的共享:
```java
package com.rain.reggie.common;

public class BaseContext {
    private static ThreadLocal<Long> idMemory = new ThreadLocal<>();

    public static void setId(Long id){
        idMemory.set(id);
    }

    public static Long getId(){
        return idMemory.get();
    }
}
```

其可行之处在于:
1. **客户端发送的每次http请求, 对应的在服务端都会分配一个新的线程来处理**.
2. 在处理过程中以下方法都属于相同的一个线程:
   LoginCheckFilter.doFilter
   EmploveeController.update
   MyMetaObjectHandler.updateFill
这一点可以在这些方法中打印线程id来验证. 对于前端的每次请求, 首先要经过过滤器验证是否已登录, 若已登录, 放行后来到控制层.
依据请求的路径和请求方式进入对应方法体, 并传入携带的参数. 例如 update 方法, 它在调用 `employeeService.updateById` 方法时会去执行 `FFMetaObjHandler.updateFill` 方法, 该方法又调用 `BaseContext` 类的 get 方法获取到 `idMemory` 中存储的 id 值.
注意一个细节, 也即调用 `setId` 的时机, 不同于 http-session, ThreadLocal 变量是针对于线程的, 因此每次请求都需要设置一遍, 而不是仅仅在登录请求(通过后)处理中设置一次. 也即应当在过滤器中设置:
```java
if (null != request.getSession().getAttribute("employee")){
    log.info("已登录");
    Long user_id = (Long) request.getSession().getAttribute("employee");
    BaseContext.setId(user_id);
    filterChain.doFilter(request, response);
    return;
}
```

插入数据, 进行功能测试:
```
obyda
切换错误
17826534892
738391474891840291
```

### 菜品分类管理
后台系统中可以管理分类信息，分类包括两种类型，分别是菜品分类和套餐分类。当我们在后台系统中添加菜品时需要选择一个菜品分类，当我们在后台系统中添加一个套餐时需要选择一个套餐分类，在移动端也会按照菜品分类和套餐分类来展示对应的菜品和套餐。
对应的表格:
```
id
type
name
sort
create_time
update_time
create_user
update_user
```
name 是分类名称, type 是分类类型, 1=菜品分类, 2=套餐分类.
sort 是在展示页面的展示顺序

#### 1. 搭建框架
包括:
1. 实体类 Category
```java
package com.rain.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;

    //类型 1 菜品分类 2 套餐分类
    private Integer type;
    //分类名称
    private String name;
    //顺序
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
```
2. Mapper
```java
package com.rain.reggie.mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
```
3. Service 接口及实现类
```java
package com.rain.reggie.service;

public interface CategoryService extends IService<Category> {
}
```
与
```java
package com.rain.reggie.service.impl;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}
```
4. controller 层
```java
@RestController
@Slf4j
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService service;
}
```

#### 2. 编写功能
##### 2-1. 分页查询
注意, 此前忽略(漏听??)了一个细节, 就是查询的总条目数量为0. 尽管页面上也能展示出记录
这是由于未设置 MybatisPlus 的配置类, 它规定了 MybatisPlus 进行分页参数填充的方式:
```java
package com.rain.reggie.config;

@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor paginationInterceptor() {
        MybatisPlusInterceptor  interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }
}
```
加入配置类后, 接着编写分页方法:
```java
@GetMapping("page")
public R<Page> page(int page, int pageSize){
    Page<Category> info = new Page<>(page, pageSize);
    LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
    wrapper.orderByAsc(Category::getSort);
    service.page(info, wrapper);
    return R.success(info);
}
```
##### 2-2 插入
注意公共字段已自动填充:
```java
@PostMapping
public R<String> insert(@RequestBody Category category){
    log.info("category={}", category);
    service.save(category);
    return R.success("添加成功");
}   
```
##### 2-3 更新 
```java
@PutMapping
public R<String> update(@RequestBody Category category){
    log.info("category={}", category);
    service.updateById(category);
    return R.success("更新成功 ");
}
```
##### 2-4 删除
菜品分类: 是指菜品所属的类别, 例如: 浙菜、湘菜、川菜等等.
套餐分类: 是指菜品所属的套餐, 例如: 亲子套餐、极意双人餐、超值单人餐等等
在对分类的类别执行删除操作时, 通常的逻辑是:
假如某些菜品包含在某个分类下, 则该分类不应被删除(无法删除)
因此删除方法需要在 Service 层自己实现
为此, 需要查询在 dish 表中是否包含该 category 的条目.

建立 dish 的框架:
```java
package com.rain.reggie.entity;

@Data
public class Dish implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    //菜品名称
    private String name;
    //菜品分类id
    private Long categoryId;
    //菜品价格
    private BigDecimal price;
    //商品码
    private String code;
    //图片
    private String image;
    //描述信息
    private String description;
    //0 停售 1 起售
    private Integer status;
    //顺序
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
```

当查询到记录时, 就抛出一个自定义业务异常:
```java
package com.rain.reggie.common;

public class BusinessException extends RuntimeException{
    public BusinessException(String mes){
        super(mes);
    }
}
```
在业务层添加 `removeWithDish` 方法:
```java
@Override
@Transactional
public void removeWithDish(Long ids) {
    LambdaQueryWrapper<Dish> dishWrapper = new LambdaQueryWrapper<>();
    dishWrapper.eq(Dish::getCategoryId, ids);
    long cnt = dishService.count(dishWrapper);
    if (cnt > 0)
        throw new BusinessException("分类冲突");
    removeById(ids);
}
```
控制层则变得简单:
```java
@DeleteMapping
public R<String> remove(Long ids){
    log.info("category to be del: {}", ids);
    service.removeWithDish(ids);
    return R.success("删除成功");
}
```
注意, 在此前的全局异常处理器中加入 BusinessException 处理逻辑:
```java
@ExceptionHandler(BusinessException.class)
public R<String> exceptionHandler(BusinessException e){
    String message = e.getMessage();
    log.error("Exception occurred: {}", message);
    return R.error(message);
}
```



