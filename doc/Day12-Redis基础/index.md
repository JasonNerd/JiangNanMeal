---
title: "Day12-Redis基础"
date: 2024-01-25T16:33:10+08:00
draft: false
tags: ["Redis", "Cache"]
categories: ["Redis"]
twemoji: true
lightgallery: true
---

Redis可以用作数据库、缓存、流媒体引擎、消息代理等。它是一种键值类型的数据库, 可用于快速数据存储与检索, 属于NoSQL.
本节学习如何在远程Linux上安装和使用Redis, 同时在本机上利用SpringBoot提供的框架操作Redis.

### 1. 安装 Redis
#### 1-1. 在远程Linux服务器上安装
来到官网:
https://redis.io/download/
点击下载链接:
https://github.com/redis/redis/archive/7.2.4.tar.gz
将下载好的压缩包上传到远程服务器. 随后解压到软件安装目录:
```sh
su root
tar -zxvf redis_xxx.tar.gz -C /usr/local
```
进入到 redis 的安装目录并执行 make 命令:
```sh
cd /usr/local/redis-7.2.4/
make
```
随后redis服务即可使用(仅在服务器端)
```sh
src/redis-server
```
#### 1-2. 修改配置使得允许远程访问
配置文件即 `redis.conf`
主要修改内容包括:
1. 消除本地绑定, 也即进行注释: `# bind 127.0.0.1 -::1`
2. 允许后台运行: `daemonize yes`
3. 开启密码验证并设置密码: `requirepass 123456`
最后保存退出, 注意到其端口为 6379. 设置防火墙:
```sh
firewall-cmd --zone=public --add-port=6379/tcp --permanent
```
先杀死原有的 redis-server 进程, 随后显式加载配置文件:
```sh
src/redis-server redis.conf
```
此时 redis 服务将开启并在后台运行, 并且理论上本机也能访问到服务.

假设本机windows下也下载了 redis , 安装目录下打开cmd并键入命令(ip为远程linux服务器的地址):
```sh
./redis-cli.exe -h 10.102.98.26 -p 6379 -a 123456
```
即可访问远程Linux上的 Redis 服务, 键入以下命令测试服务可用性:
```sh
keys *
```

### Redis 的常见命令
#### String 操作
https://www.runoob.com/redis/redis-strings.html

序号 | 命令及描述
:-|:-
1	|`SET key value`, 设置指定 key 的值。
2	|`GET key`, 获取指定 key 的值。
3	|`GETRANGE key start end`, 返回 key 中字符串值的子字符

```sh
redis 127.0.0.1:6379> SET runoobkey redis
OK
redis 127.0.0.1:6379> GET runoobkey
"redis"
```

#### Hash 操作
https://www.runoob.com/redis/redis-hashes.html
Redis hash 是一个 string 类型的 field（字段） 和 value（值） 的映射表，hash 特别适合用于存储对象。

序号 | 命令及描述
:-|:-
1| `HSET key field value`, 将哈希表 key 中的字段 field 的值设为 value 
2| 	`HMSET key field1 value1 [field2 value2 ]`, 同时将多个 field-value (域-值)对设置到哈希表 key 中
3|`HDEL key field1 [field2]`, 删除一个或多个哈希表字段
4|`HEXISTS key field`, 查看哈希表 key 中，指定的字段是否存在
5|`HGET key field`, 获取存储在哈希表中指定字段的值
6|`HGETALL key`, 获取在哈希表中指定 key 的所有字段和值
7|`HKEYS key`, 获取哈希表中的所有字段
8|`HVALS key`, 获取哈希表中所有值

```sh
127.0.0.1:6379>  HMSET runoobkey name "redis tutorial" description "redis basic commands for caching" likes 20 visitors 23000
OK
127.0.0.1:6379>  HGETALL runoobkey
1) "name"
2) "redis tutorial"
3) "description"
4) "redis basic commands for caching"
5) "likes"
6) "20"
7) "visitors"
8) "23000"
```

#### List 操作
Redis列表是简单的字符串列表，按照插入顺序排序。你可以添加一个元素到列表的头部（左边）或者尾部（右边）
命令 | 描述
:-|:-
`LPUSH key value1 [value2]` | 将一个或多个值插入到列表头部
`LRANGE key start stop` | 获取列表指定范围内的元素
`LINDEX key index` | 通过索引获取列表中的元素
`LPOP key` | 移出并获取列表的第一个元素
`RPUSH key value1 [value2]` | 在列表中添加一个或多个值到列表尾部
`RPOP key` | 移除列表的最后一个元素，返回值为移除的元素。
`LTRIM key start stop` | 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除。
```sh
redis 127.0.0.1:6379> LPUSH runoobkey redis
(integer) 1
redis 127.0.0.1:6379> LPUSH runoobkey mongodb
(integer) 2
redis 127.0.0.1:6379> LPUSH runoobkey mysql
(integer) 3
redis 127.0.0.1:6379> LRANGE runoobkey 0 -1

1) "mysql"
2) "mongodb"
3) "redis"

```

#### Set 操作
Redis 的 Set 是 String 类型的无序集合。集合成员是唯一的，这就意味着集合中不能出现重复的数据。
集合对象的编码可以是 intset 或者 hashtable。
Redis 中集合是通过哈希表实现的，所以添加，删除，查找的复杂度都是 O(1)。

命令 | 描述
:-|:-
`SADD key member1 [member2]`|向集合添加一个或多个成员
`SCARD key`|获取集合的成员数
`SDIFF key1 [key2]`|返回第一个集合与其他集合之间的差异。
`SDIFFSTORE destination key1 [key2]`|返回给定所有集合的差集并存储在 destination 中
`SINTER key1 [key2]`|返回给定所有集合的交集
`SINTERSTORE destination key1 [key2]`|返回给定所有集合的交集并存储在 destination 中
`SISMEMBER key member`|判断 member 元素是否是集合 key 的成员
`SMEMBERS key`|返回集合中的所有成员
`SMOVE source destination member`|将 member 元素从 source 集合移动到 destination 集合
`SPOP key`|移除并返回集合中的一个随机元素
`SRANDMEMBER key [count]`|返回集合中一个或多个随机数
`SREM key member1 [member2]`|移除集合中一个或多个成员
`SUNION key1 [key2]`|返回所有给定集合的并集
`SUNIONSTORE destination key1 [key2]`|所有给定集合的并集存储在 destination 集合中

```sh
redis 127.0.0.1:6379> SADD runoobkey redis
(integer) 1
redis 127.0.0.1:6379> SADD runoobkey mongodb
(integer) 1
redis 127.0.0.1:6379> SADD runoobkey mysql
(integer) 1
redis 127.0.0.1:6379> SADD runoobkey mysql
(integer) 0
redis 127.0.0.1:6379> SMEMBERS runoobkey

1) "mysql"
2) "mongodb"
3) "redis"
```

#### sorted set 操作
Redis 有序集合和集合一样也是 string 类型元素的集合,且不允许重复的成员。

不同的是每个元素都会关联一个 double 类型的分数。redis 正是通过分数来为集合中的成员进行从小到大的排序。

有序集合的成员是唯一的,但分数(score)却可以重复。

集合是通过哈希表实现的，所以添加，删除，查找的复杂度都是 O(1)。 

命令 | 描述
:-|:-
`ZADD key score1 member1 [score2 member2]`|向有序集合添加一个或多个成员，或者更新已存在成员的分数
`ZCARD key`|获取有序集合的成员数
`ZCOUNT key min max`|计算在有序集合中指定区间分数的成员数
`ZINCRBY key increment member`|有序集合中对指定成员的分数加上增量 increment
`ZINTERSTORE destination numkeys key [key ...]`|计算给定的一个或多个有序集的交集并将结果集存储在新的有序集合 destination 中
`ZLEXCOUNT key min max`|在有序集合中计算指定字典区间内成员数量
`ZRANGE key start stop [WITHSCORES]`|通过索引区间返回有序集合指定区间内的成员
`ZRANGEBYLEX key min max [LIMIT offset count]`|通过字典区间返回有序集合的成员
`ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT]`|通过分数返回有序集合指定区间内的成员
`ZRANK key member`|返回有序集合中指定成员的索引
`ZREM key member [member ...]`|移除有序集合中的一个或多个成员
`ZREMRANGEBYLEX key min max`|移除有序集合中给定的字典区间的所有成员
`ZREMRANGEBYRANK key start stop`|移除有序集合中给定的排名区间的所有成员
`ZREMRANGEBYSCORE key min max`|移除有序集合中给定的分数区间的所有成员
`ZREVRANGE key start stop [WITHSCORES]`|返回有序集中指定区间内的成员，通过索引，分数从高到低
`ZREVRANGEBYSCORE key max min [WITHSCORES]`|返回有序集中指定分数区间内的成员，分数从高到低排序
`ZREVRANK key member`|返回有序集合中指定成员的排名，有序集成员按分数值递减(从大到小)排序
`ZSCORE key member`|返回有序集中，成员的分数值
`ZUNIONSTORE destination numkeys key [key ...]`|计算给定的一个或多个有序集的并集，并存储在新的 key 中

### Springboot 中使用 redis
新建一个空的 Springboot 项目, 加入依赖:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

```
写好配置:
```yml
spring:
  redis:
    host: 192.168.79.133
    database: 0
    port: 6379
    password: 123456
    jedis:
      pool:
        max-active: 8
        max-wait: 1ms
        max-idle: 4
        min-idle: 0
```
在测试类中写测试方法:
```java
@Autowired
RedisTemplate redisTemplate;

@Test
void testString() {
    redisTemplate.opsForValue().set("time", "noon");
    String now = (String) redisTemplate.opsForValue().get("time");
    System.out.println(now);
}

@Test
void testHash(){
    redisTemplate.opsForHash().put("java", "version", 17);
    redisTemplate.opsForHash().put("java", "alg", "trace back");
    redisTemplate.opsForHash().put("java", "salary", 100);
    redisTemplate.opsForHash().put("java", "concurrency", true);
    Integer version = (Integer) redisTemplate.opsForHash().get("java", "version");
    System.out.println(version);
}
```



