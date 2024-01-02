---
title: "瑞吉外卖 - Day03-管理员后台开发（二）新增员工与条件分页查询"
date: 2024-01-02T20:18:45+08:00
draft: false
tags: ["Reggie", "SpringBoot", "JavaWeb"]
categories: ["Reggie"]
twemoji: true
lightgallery: true
---

新增员工, 全局异常处理器, 条件分页查询

### 1. 新增员工
```java
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
```

测试数据, 其中第四位重复添加(也即账户名一样)
```git
weqfaker
赖思伟
13131421434
481930010138120344

qwoenf
奥韦若飞
13139481434
481930010138120344

waieb
沃尔夫
13131418023
481930010138120344

weqfaker
赖思伟
13131421434
481930010138120344
```

添加第四位时出现异常, 因此需要添加全局异常处理器

### 2. 全局异常处理器
添加全局异常处理器:
```java
package com.rain.reggie.common;

@ControllerAdvice(annotations = RestController.class)
@ResponseBody
@Slf4j
public class RegExceptionHandler {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException e){
        String message = e.getMessage();
        log.info("异常: {}", message);
        if (message.contains("Duplicate entry")){
            String acc = message.split(" ")[2];
            return R.error("用户已存在"+acc);
        }
        return R.error("操作失败");
    }
}
```
需要注意的是:
1. `@ControllerAdvice` 注解用于指明该异常处理类接管的方法范围, 例如使用了 `RestController` 注解的类.
2. `@ResponseBody` 表明该类的方法会有返回值(响应).
3. 异常处理方法需要 `@ExceptionHandler` 修饰, 其中指明需要处理的具体异常类型.


### 3. 条件分页查询
在 MybalisPlus 的帮助下, 他变得十分简洁, 先查看前端请求:
http://localhost:8080/employee/page?page=1&pageSize=10&name=%E5%BC%A0

请求路径:
"/page"

请求参数:
`int page, int pageSize, String name`

代码如下:
```java
@GetMapping("/page")
// 非rest风格
public R<Page> query(int page, int pageSize, String name){
    LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.like(!StringUtils.isEmpty(name), Employee::getName, name);
    queryWrapper.orderByDesc(Employee::getUpdateTime);
    Page<Employee> pageInfo = new Page<>(page, pageSize);
    employeeService.page(pageInfo, queryWrapper);
    log.info("Page: {}", pageInfo);
    return R.success(pageInfo);
}
```

先创建 LambdaQueryWrapper, 指定查询函数及其参数, 注意添加非空条件.
接着可以添加一个排序操作 orderByDesc
创建分页参数实体 pageInfo, 调用 service 层执行查询, 信息将保存在 pageInfo.
返回响应.

### 4. 更新员工
这包括更新员工信息, 更新员工状态.

#### 依据 id 查询员工信息进行回显
请求路径示例如下:
http://localhost:8080/employee/1742161636414681000
方式为 GET.
```java
@GetMapping("/{empId}")
public R<Employee> getById(@PathVariable Long empId){
    LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Employee::getId, empId);
    Employee emp = employeeService.getById(empId);
    return R.success(emp);
}
```

重启应用, 点击编辑按钮跳转到编辑页面, 可以看到此时请求已发送且响应200 ok.
但此时页面上并未显示出员工信息.
查看控制台日志:
```log
拦截到请求/employee/1742161636414681000
...
...
==> Parameters: 1742161636414681000(Long)
<==      Total: 0
```
仔细观察, 发现查询参数 id 不正确, 这是由于数据类型为整数, 前端存储时导致精度丢失(尾部的000).
因此可以考虑传给前端保存时把 id 转为字符串, 而在传给后端时再转为整型
为了达到这个目的, 我们需要 **扩展底层默认的对象转换器**, 使得数据在存储时转为字符串防止精度丢失
同时取出时又自动转为整数, 这样的配置从 service/controller 层来看是透明的: 也即代码无需变动.

```java
package com.rain.reggie.common;

public class JacksonObjectMapper extends ObjectMapper {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public JacksonObjectMapper() {
        super();
        //收到未知属性时不报异常
        this.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        //反序列化时，属性不存在的兼容处理
        this.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        SimpleModule simpleModule = new SimpleModule()
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)))
                .addSerializer(BigInteger.class, ToStringSerializer.instance)
                .addSerializer(Long.class, ToStringSerializer.instance)
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));
        //注册功能模块 例如，可以添加自定义序列化器和反序列化器
        this.registerModule(simpleModule);
    }
}
```
如上, 主要针对几种常见的数据类型进行了重写, 整数类型则转为字符串类型.
```java
.addSerializer(BigInteger.class, ToStringSerializer.instance)
.addSerializer(Long.class, ToStringSerializer.instance)
```

另外, 需要在配置类中重写 `extendMessageConverters` 方法, 添加自定义的对象转换器, 使其生效:
```java
@Configuration
@Slf4j
public class WebMvcConfig extends WebMvcConfigurationSupport {

    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("开始消息转换 ... ...");
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new JacksonObjectMapper());
        converters.add(0, converter);
    }
}
```
重启应用, 刷新页面. 发现报错 404 NOT FOUND.
静态页面资源忽然就访问不到了, 还记得第一小节的提醒吗, 前端资源页面必须放在 static 目录下.
否则访问不到, 需要手动映射. 因此解决方法是添加资源映射函数:
```java
@Configuration
@Slf4j
public class WebMvcConfig extends WebMvcConfigurationSupport {

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("开始消息转换 ... ...");
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new JacksonObjectMapper());
        converters.add(0, converter);
    }
}
```

配置好后重新进行编辑操作, 发现页面数据可以回显了.


#### 依据 id 执行实际更新操作
点击编辑页面的保存按钮, 在浏览器开发者页面可以看到:
请求路径:
http://localhost:8080/employee
请求方式:
PUT
请求参数:
```json
{
  "id": "1742161636414681089",
  "username": "waieb",
  "name": "沃尔夫",
  "password": "e10adc3949ba59abbe56e057f20f883e",
  "phone": "13131418023",
  "sex": "0",
  "idNumber": "481930010138120344",
  "status": 1,
  "createTime": "2024-01-02 20:31:17",
  "updateTime": "2024-01-02 20:31:17",
  "createUser": "1",
  "updateUser": "1"
}
```
据此就可以编写后台 controller 方法了
```java
@PutMapping("")
public R<String> update(HttpServletRequest request, @RequestBody Employee employee){
    log.info("更新员工信息: {}", employee.toString());
    Long currentUser = (Long) request.getSession().getAttribute("employee");
    employee.setUpdateUser(currentUser);
    employee.setUpdateTime(LocalDateTime.now());
    employeeService.updateById(employee);
    return R.success("更新成功");
}
```
重启应用, 此时更新操作顺利执行!
不难发现, 目前似乎很多业务逻辑的操作也都放在的 controller 层, 后续还需要进一步调整.

