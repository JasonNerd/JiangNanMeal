---
title: "Day13-Reggie-版本管理与缓存优化"
date: 2024-01-29T11:15:29+08:00
draft: false
tags: ["git", "redis", "SpringBoot", "JavaWeb"]
categories: ["Reggie"]
twemoji: true
lightgallery: true
---

使用 git 来进行版本管理, 不同版本的代码放在不同的分支. 同时, 本次还将使用 redis, SpringCache 等技术, 使用缓存对项目进行优化.

### 1. git 版本控制
使用 IDEA 界面进行控制, 具体的, 先清理代码, 将第一版(该版本完成了后台管理的大部分功能)作为main分支, commit and push 到远程. 本地新开 version1.0 分支并 push 到远程, 将第一版代码删去, 使用第二版进行覆盖, 该版本完善了前台的大部分功能, 随后 commit and push. 紧接着新开分支 version1.1, push 到远程, 该版本将更新关于项目优化的代码内容.

### 2. 前端菜品展示的缓存
引入 redis 依赖, 配置 redis 服务的 ip port 等:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```
```yml
spring:
  redis:
    host: 172.17.2.94
    port: 6379
    password: root@123456
    database: 0
    jedis:
      pool:
        max-active: 8
        max-wait: 1ms
        max-idle: 4
        min-idle: 0
```

#### 2-1. 代码思路及实现
前面我们已经实现了移动端菜品查看功能，对应的服务端方法为 DishControler 的 list 方法，此方法会根据前端提交的查询条件进行数据库查询操作。在高并发的情况下，频繁查询数据库会导致系统性能下降，服务端响应时间增长。现在需要对此方法进行缓存优化，提高系统的性能。具体的实现思路如下:
1、改造 DishController 的 list 方法，先从 Redis 中获取菜品数据，如果有则直接返回，无需查询数据库; 如果没有则查询数据库，并将查询到的菜品数据放入 Redis 。
2、改造 DishController 的 save 和 update 方法，加入清理缓存的逻辑

注意事项
在使用缓存过程中，要注意保证数据库中的数据和缓存中的数据一致，如果数据库中的数据发生变化，需要及时清理缓存数据。

对于 DishControler 的 list 方法, 加入代码(注意 dishDtoList 声明提前):
```java
// 使用 redis 缓存, 先查看该分类的信息是否已被查询
log.info("尝试从 redis 读取缓存");
String dishKey = "Dish_"+categoryId;
dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(dishKey);
if (dishDtoList != null) {
    log.info("从 redis 读取缓存成功");
    return R.success(dishDtoList);
}
log.info("缓存不命中, 从数据库查询");
...
redisTemplate.opsForValue().set(dishKey, dishDtoList, 60, TimeUnit.MINUTES);    // 设置缓存
```
对于 DishControler 的 insert 方法 和 delete 方法, 加入代码(注意 dishDtoList 声明提前):
```java
Set keys = redisTemplate.keys("Dish*");
redisTemplate.delete(keys);
```

### 3. SpringCache
Spring Cache是一个框架，实现了基于注解的缓存功能，只需要简单地加一个注解，就能实现缓存功能

Spring Cache提供了一层抽象，底层可以切换不同的cache实现。具体就是通过CacheManager接口来统一不同的缓存技术。

CacheManaqer是Spring提供的各种缓存技术抽象接口，针对不同的缓存技术需要实现不同的CacheManager:

CacheManager | 描述
:-|:-
EhCachecacheManager | 使用 EhCache 作为缓存技术
GuavaCacheManage | 使用 Google 的 Guavacache 作为缓存技术
RediscacheManager | 使用Redis作为缓存技术

#### 3-1. Spring Cache 常用注解
注解 | 说明
:-| :-
@Enablecaching | 开启缓存注解功能
@Cacheable | 在方法执行前spring先查看缓存中是否有数据，如果有数据，则直接返回缓存数据，若没有数据，调用方法并将方法返回值放到缓存中
@CachePut  | 将方法的返回值放到缓存中
@CacheEvict | 将一条或多条数据从缓存中删除

在spring boot项目中，使用缓存技术只需在项目中导入相关缓存技术的依赖包，并在启动类上使用`@Enablecaching`开启缓存支持即可
例如，使用Redis作为缓存技术，只需要导入Spring data Redis的maven坐标即可.


#### 3-2. @CachePut
`@CachePut(value ="value",key ="key")`
CachePut: 将方法返回值放入缓存
value: 缓存的名称，每个缓存名称下面可以有多个key
key: 缓存的key

通常, 可以在插入类的方法中加上该注解, 例如:
```java
@CachePut(value="userCache", key="#user.id")
@PostMapping
public User save(User user){
    userService.save(user);
    return user;
}
```
注意 key 的写法, `#` 符号可以使得字符串可以代表某一变量, 例如此处得到了插入数据的id(返回结果前)
另外一点是, User 类还必须实现 Serializable 接口, 使得函数返回值 user 可以被序列化和反序列化.
key 还有一种写法, 例如: `#result.id`, 此处 result 统一表示返回值.

#### 3-3. @CacheEvict
`@CacheEvict(value ="userCache", key = "#id")`
CacheEvict: 删除 userCache 下键值为 id 的缓存条目
通常注解在更新或删除请求处理函数上
```java
// @CacheEvict(value =userCache",key ="#root.args[0]")
// @CacheEvict(value =userCache",key ="#p0")
@CacheEvict(value ="userCache" ,key = "#id")
@DeleteMapping("/{id}")
public void delete(@PathVariable Longs id){
    userService.removeById(id);
}

@CacheEvict(value="userCache", key="#result.id")
@PutMapping
public User update(User user){
    userService.updateById(user);
    return user;
}
```
注意 key 的写法, 注释中的两种写法都表示参数列表中的第一个参数值.
该注解支持条件执行:
#### 3-4. @Cacheable
在方法执行前spring先查看缓存中是否有数据，如果有数据，则直接返回缓存数据，若没有数据，调用方法并将方法返回值放到缓存中
```java
@Cacheable(value ="userCache", key = "#id")
@GetMapping("/{id}")
public User getById(@PathVariable id){
    Long User user = userService.getById(id);
    return user;
}
```
该注解通常用于查询类的方法

Cacheable 支持条件缓存, 关键字包含 condition 和 unless
`@Cacheable(value="userCache", key ="#id", unless="#result == null")`
condition: 满足条件时才缓存数
unless: 满足条件则不缓存
这里含义是若 userCache 缓存中包含键值为 #id(动态值) 数据条目, 就返回缓存数据. 否则进行缓存, 除非查询结果为空


### 4. 套餐缓存
使用基于 redis 的 SpringCache 技术来做关于套餐数据的缓存.
需要的依赖是:
`spring-boot-starter-data-redis`
`spring-boot-starter-cache`
基本步骤如下:
1. 导入Spring cache和Redis相关maven坐标
2. 在application.yml中配置缓存数据的过期时间
```yml
spring:
  redis:
    host: 192.168.30.226
    database: 0
    port: 6379
    password: 123456
    jedis:
      pool:
        max-active: 8
        max-wait: 1ms
        max-idle: 4
        min-idle: 0
  cache:
    redis:
      time-to-live: 1800000
```
3. 在启动类上加入@Enablecaching注解，开启缓存注解功能
```java
@SpringBootApplication
@ServletComponentScan
@EnableCaching
public class ReggieApplication {
    ...
}
```
3. 注意到前端的关于套餐的查询方法位于: `SetmealController.list()`, 为它加上 `@Cacheable` 注解:
```java
@Cacheable(value = "setmeal", key = "#setmeal.categoryId")
```
注意到函数返回类型为 R, 为此需要使 R 类实现 Serializable接口.
4. 为 SetmealController 的关于套餐修改添加等的请求处理方法添加 `@CacheEvict` 注解
```java
@CacheEvict(value = "setmeal")
```

#### 使用 Aliyun服务器和 Navicat16.3
前者可以用于托管服务, 不再使用虚拟机自导自演, 新人有一个年度优惠(一年的期限, 到期了之后可以尝试海外的更便宜的服务器). 后者用于连接、访问、可视化各种数据库, 16.3专业版可以仅使用一个软件来访问多种数据库服务, 例如 redis/mysql/mongodb 等等(该软件付费, 具体下载安装可以参考网络教程).
与此前相同, 需要为远程服务器安装 java/tomcat/redis/mysql, 可以参考此前的教程, 也可以参考网络教程.
另外, 为了使服务器的 mysql 服务可在远程访问, 还需要一些步骤:
1. 命令行登录mysql: `mysql -uroot -p`
2. 查看当前表中的数据库: `show databases;`
3. 进入到mysql数据库: `use mysql;`
4. 查看当前数据库下，用户表的数据(主机,用户,密码): `select Host, User from user;`
5. 修改user表主机, 修改host值（以通配符%的内容增加主机/IP地址）`update user set host = '%' where user ='root';`
6. 刷新MySQL的系统权限相关表 `flush privileges;`

#### 代码提交与推送
查看当前分支, 应为 v1.2版本, 提交和推送当前变动代码, 主要是 redis 和 SpringCache 的引入. 下一版本将引入 mysql 主从库读写分离以及Nginx等.
