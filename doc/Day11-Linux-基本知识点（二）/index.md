---
title: "Day11-Linux-基本知识点（二）"
date: 2024-01-16T17:06:09+08:00
draft: false
tags: ["Linux", "Command"]
categories: ["Linux"]
twemoji: true
lightgallery: true
---


### 1. 打包压缩命令
打包压缩命令 tar

作用:对文件进行打包、解包、压缩、解压

语法: tar[-zcxvf] fileName [files]

包文件后缀为.tar 表示只是完成了打包，并没有压缩
包文件后缀为.tar.gz 表示打包的同时还进行了压缩
说明:
-z: z代表的是gzip，通过gzip命令处理文件，gzip可以对文件压缩或者解压
-c: c代表的是create，即创建新的包文件
-x: x代表的是extract，实现从包文件中还原文件
-v: v代表的是verbose，显示命令的执行过程
-f: f代表的是file，用于指定包文件的名称

常用选项组合
```bash
tar -cvf test.tar test 
tar -zcvf test.tar.gz test
tar -xvf test.tar
tar -zxvf test.tar.gz
```

### 2. 文本编辑命令
文本编辑命令 vi/vim
作用: vi命令是Linux系统提供的一个文本编辑工具，可以对文件内容进行编辑，类似于Windows中的记事本
语法: `vi fileName`

说明:
1、vim是从vi发展来的一个功能更加强大的文本编辑工具，在编辑文件时可以对文本内容进行着色，方便我们对文件进行编辑处理，所以实际工作中vim更加常用。
2、要使用vim命令，需要我们自己完成安装。可以使用下面的命令来完成安装: `yum install vim`

Linux常用命令
文本编辑命令 vim
作用:对文件内容进行编辑，vim其实就是一个文本编辑器语法: vim fileName
说明:
1、在使用vim命令编辑文件时，如果指定的文件存在则直接打开此文件。如果指定的文件不存在则新建文件。

2、vim在进行文本编辑时共分为三种模式，分别是**命令模式(Command mode)**, **插入模式(lnsert mode)**, **底行模式(Last line mode)**. 这三种模式之间可以相互切换。我们在使用vim时一定要注意我们当前所处的是哪种模式。

#### 命令模式
命令模式下可以查看文件内容、移动光标, 通过vim命令打开文件后，默认进入命令模式.
另外两种模式需要首先进入命令模式, 才能进入彼此.

#### 插入模式 
插入模式下可以对文件内容进行编辑
在命令模式下按下`[i,a,o]`任意一个，可以进入插入模式。进入插入模式后，下方会出现 `[insert]` 字样. 
在插入模式下按下ESC键，回到命令模式.

#### 底行模式 
底行模式下可以通过命令对文件内容进行查找、显示行号.
在命令模式下按下`[:,/]`任意一个，可以进入底行模式
通过`/`方式进入底行模式后, 可以对文件内容进行查找
通过`:`方式进入底行模式后, 可以输入wq (保存并退出) 、q!(不保存退出)、set nu (显示行号)

### 4-5. 查找命令
#### find
作用: 在指定目录下查找文件
语法: `find dirName -option fileName`
举例:
```bash
find .-name "*java"
find /itcast -name "*.java"
```
在当前目录及其子目录下查找.java结尾文件
在/itcast目录及其子目录下查找.iava结尾的文件

#### grep
作用:从指定文件中查找指定的文本内容
语法: grep word fileName
举例:
```bash
grep Hello HelloWorld.java
grep hello *.java
```
查找HelloWorld.java文件中出现的Hello字符串的位置 
查找当前目录中所有java结尾的文件中包含hello字符串的位置

### 5. 软件包安装
二进制发布包安装
软件已经针对具体平台编译打包发布，只要解压，修改配置即可

rpm安装
软件已经按照redhat的包管理规范进行打包，使用rpm命令进行安装，不能自行解决库依赖问题

yum安装
一种在线软件安装方式，本质上还是rpm安装，自动下载安装包并安装，安装过程中自动解决库依赖问题

源码编译安装
软件以源码工程的形式发布，需要自己编译打包
#### 安装 Java
下载 jdk17, 上传到服务器
```bash
https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
https://download.oracle.com/java/17/archive/jdk-17.0.9_linux-x64_bin.tar.gz
```
解压到 `usr/local/java` 目录
```
mkdir /usr/local/java/
tar -xzvf /software/java/jdk-17.0.9_linux-x64_bin.tar.gz -C /usr/local/java/
```
编辑环境变量:
```bash
vim /etc/profile
```
追加内容:
```bash
export JAVA_HOME=/usr/local/java/jdk-17.0.2
export PATH=$PATH:$JAVA_HOME/bin;
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar;
```
使环境变量生效:
```bash
source /etc/profile
```
注意如果权限缺失:
```bash
su - root
```
退出时: `Ctrl + D`

#### 安装 tomcat
https://tomcat.apache.org/download-10.cgi
下载对应安装包并上传至服务器
解压到 `/usr/local/` 目录 
进入 tomcat 的 bin 目录
启动 tomcat 服务: `sh startup.sh`

可以使用如下的命令组合查看 tomcat 进程:
```bash
ps -ef | grep tomcat
```

使用本地电脑访问:
```
http://192.168.79.128:8080/
```
其中 ip 地址为远程服务器的地址(或者虚拟机的ip地址), 查看是否可以访问到页面
若不能, 则可能是 **防火墙** 的作用.

##### 防火墙操作
查看防火墙状态: `systemctl status firewalld` or `firewall-cmd --state` 
暂时关闭防火墙: `systemctl stop firewalld` 
永久关闭防火墙: `systemctl disable firewalld` 
开启防火墙: `systemctl start firewalld` 
开放指定端口: `firewall-cmd --zone=public --add-port=8080/tcp --permanent`
关闭指定端口: `firewall-cmd --zone=public --remove-port=8080/tcp --permanent`
立即生效: `firewall-cmd --reload`
查看开放的端口: `firewall-cmd --zone=public --list-ports`
注意:
1、systemctl是管理Linux中服务的命令，可以对服务进行启动、停止、重启、查看状态等操作 
2、firewall-cmd是Linux中专门用于控制防火墙的命令
3、为了保证系统安全，服务器的防火墙不建议关闭 


执行命令:
```bash
firewall-cmd --zone=public --add-port=8080/tcp --permanent
firewall-cmd --reload
firewall-cmd --zone=public --list-ports
```

刷新页面, 发现页面可以正常加载.

##### 停止Tomcat服务
1. 运行Tomcat的bin目录中提供的停止服务的脚本文件shutdown.sh
```sh
sh shutdown.sh
./shutdown.sh
```
2. 结束Tomcat进程查看Tomcat进程，获得进程id
执行命令结束进程 `kill -9 7742`
注意, kill命令是Linux提供的用于结束进程的命令，-9表示强制结束


### 安装 MySQL 服务

先查看系统是否包含 mysql 服务
```sh
rpm -qa | grep mysql
```
查询当前系统中安装的名称带mariadb的软件
```sh
rpm -ga | grep mariadb
```
如果这两个程序存在则需要先卸载, 否则会产生冲突
```sh
rpm -e --nodeps mariadb-libs-5.5.60-1.el7_5.x86_64
```
将MySQL安装包上传到Linux并解压
```sh
mkdir /usr/local/mysql
tar -zxvf mysql-5.7.25-1.el7.x86 64.rpm-bundle.tar.gz -C /usr/local/mysql
```
按照顺序安装rpm软件包
```sh
rpm -ivh mysql-community-common-5.7.25-1.el7.x86_64.rpm
rpm -ivh mysql-community-libs-5.7.25-1.el7.x86_64.rpm
rpm -ivh mysql-community-devel-5.7.25-1.el7.x86_64.rpm
rpm -ivh mysql-community-libs-compat-5.7.25-1.el7.x86_64.rpm
rpm -ivh mysql-community-client-5.7.25-1.el7.x86_64.rpm
yum install net-tools
rpm -ivh mysql-community-server-5.7.25-1.el7.x86_64.rpm
```
说明
1:安装过程中提示缺少net-tools依赖，使用yum安装说明
2: 可以通过指令升级现有软件及系统内核
yum update

查看 mysql 服务状态, 启动 mysql 服务:
```sh
systemctl status mysgld
systemctl start mysqld
```
可以设置开机时启动 mysql 服务，避免每次开机启动 mysql: `systemctl enable mysqld`
查看已经启动的服务: `netstat -tunlp`

##### 登录MySQL数据库，查阅临时密码
```sh
cat /var/log/mysqld.log
cat /var/log/mysqld.log | grep password
```
注意事项, 冒号后面的是密码, 注意空格
登录MySOL，修改密码，开放访问权限: `mysql -uroot -p`
```sh
set global validate_password_length=4;      # 设置密码长度最低位数
set global validate_password_policy=LOW;    # 设置密码安全等级低，便于密码可以修改成root
set password = password('root');            # 设置密码为root
# 开启访问权限
grant all on *.* to 'root'@'%' identified by 'root';
flush privileges;
```
测试MySQL数据库是否正常工作: `show databases;`

