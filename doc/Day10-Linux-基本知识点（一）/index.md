---
title: "Day10-Linux-基本知识点（一）"
date: 2024-01-13T16:15:28+08:00
draft: false
tags: ["Linux", "Command"]
categories: ["Linux"]
twemoji: true
lightgallery: true
---

不同应用领域的主流操作系统, 例如: 桌面操作系统, 服务器操作系统, 移动设备操作系统, 嵌入式操作系统
桌面操作系统例如: Windows(用户数量最多), Mac os(操作体验好，办公人士首选), Linux (用户数量少)
服务器操作系统例如: UNIX (安全、稳定、付费), Linux(安全、稳定、免费、占有率高), windows Server (付费、占有率低)
移动设备操作系统例如: Android (基于 Linux、开源，主要用于智能手机、平板电脑和智能电视), ios( 苹果公司开发、不开源，用于苹果公司的产品，例如: iphone、 iPad)
嵌入式操作系统: Linux (机顶盒、路由器、交换机)

### 1. Linux简介
Linux系统历史
时间:1991年
地点:芬兰赫尔辛基大学
人物: Linus Torvalds (21岁)
语言: C语言、汇编语言
logo:企鹅
特点:免费、开源、多用户、多任务

Linux系统分为内核版和发行版
内核版
由Linus Torvalds及其团队开发、维护
免费、开源
负责控制硬件
发行版
基于Linux内核版进行扩展由各个Linux厂商开发、维护有收费版本和免费版本

### 2. Linux 安装
#### 安装方式
物理机安装: 直接将操作系统安装到服务器硬件上
虚拟机安装: 通过虚拟机软件安装
虚拟机 (Virtual Machine) 指通过软件模拟的具有完整硬件系统功能、运行在完全隔离环境中的完整计算机系统
例如: VMWare, VirtualBox, VMLite WorkStation, Qemu, HopeddotVoS

#### 虚拟机安装
可以自行安装 VMWare17, 同时安装 Centos 操作系统, 注意:
如若在安装 VMWare 时出现如下警告, 应退出安装
```log
安装程序检测到主机启用了Hyper-V 或 Device/Credential Guard。
要在启用了Hyper-V 或 Device/Credential Guard的主机上运行VMware Workstation xxPlayer，
请在主机上通过“打开或关闭Windows功能”安装Windows hypervisor platform（WHP），
或者从系统中移除Hyper-V角色。
```
一个可行的办法是使用
https://www.microsoft.com/en-us/download/details.aspx?id=53337

如果路径失效了，可以自己百度，下载下来的东西叫 dgreadiness_v3.6.zip
这个时候，使用管理员打开PowerShell,切换到上面的解压目录，然后执行：
```cmd
.\DG_Readiness_Tool_v3.6.ps1 -Disable -AutoReboot
```
使用如下命令:
```cmd
win+r msinfo32
```
查看 基于虚拟化的安全性, 若为未启用则表明成功, 随后可再次执行 VMWare17 安装程序.
https://blog.csdn.net/weixin_44537885/article/details/130985414

开启虚拟机后, 可使用 finalshell 连接主机(相当于连接到远程服务器)

### 3. Linux 目录介绍

目录 | 描述
:-|:-
bin |存放二进制可执行文件
boot |存放系统引导时使用的各种文件
dev |存放设备文件
etc |存放系统配置文件
home |存放系统用户的文件
lib |存放程序运行所需的共享库和内核模块
opt |额外安装的可选应用程序包所放置的位置
root |超级用户目录
sbin |存放二进制可执行文件，只有root用户才能访问
tmp |存放临时文件
usr |存放系统应用程序
var |存放运行时需要改变数据的文件，例如日志文件

### 4. Linux 常用命令
命令 | 对应英文 | 描述
:-|:-|:-|
ls | list | 列举目录项
pwd | print work directory | 打印工作目录
cd [目录名] | change directory | 切换目录
touch [文件名] | touch | 如果指定的文件不存在则新建文件
mkdir [目录名] | make directory | 创建目录
rm [文件名] | remove | 删除指定文件

```bash
[root226@localhost ~]$ ls
Desktop  Documents  Downloads  Music  Pictures  Public  Templates  Videos
[root226@localhost ~]$ pwd
/home/root226
[root226@localhost ~]$ cd Desktop/
[root226@localhost Desktop]$ mkdir test116
[root226@localhost Desktop]$ cd test116/
[root226@localhost test116]$ touch abc
[root226@localhost test116]$ ls
abc
[root226@localhost test116]$ rm abc 
[root226@localhost test116]$ ls
[root226@localhost test116]$ cd ..
[root226@localhost Desktop]$ ls
test116
[root226@localhost Desktop]$ 
```
Linux命令使用技巧
* Tab键自动补全
* 连续两次Tab键，给出操作提示
* 使用上下箭头快速调出曾经使用过的命令
* 使用clear命令或者Ctrl+l快捷键实现清屏
在执行Linux命令时，提示信息如果显示为乱码，需要修改Linux的编码:
```
echo 'LANG="en US.UTF-8"' >> /etc/profile source /etc/profile
```

Linux命令格式

command [-options][parameter]
说明:
* command: 命令名
* [-options]: 选项，可用来对命令进行控制，也可以省略
* [parameter]: 传给命令的参数，可以是零个、一个或者多个
注意:
[]代表可选
命令名、选项、参数之间有空格进行分隔
```bash
[root226@localhost test116]$ touch 12.txt 23.txt 45.txt
[root226@localhost test116]$ ls -l
总用量 0
-rw-rw-r--. 1 root226 root226 0 1月  15 18:55 12.txt
-rw-rw-r--. 1 root226 root226 0 1月  15 18:55 23.txt
-rw-rw-r--. 1 root226 root226 0 1月  15 18:55 45.txt
[root226@localhost test116]$ ls -a
.  ..  12.txt  23.txt  45.txt
```

#### 4-1. 文件目录操作命令
##### ls
文件目录操作命令 ls
作用: 显示指定目录下的内容
语法: ls [-al] [dir]
说明:
-a 显示所有文件及目录(开头的隐藏文件也会列出)[除文件名称外，同时将文件型态(**d表示目录，-表示文件**)、权限、拥有者、文件大小等信息详细列出
注意:
由于我们使用ls命令时经常需要加入-l选项，所以Linux为`lS -l`命令提供了一种简写方式，即`ll`

##### cd
文件目录操作命令 cd
作用:用于切换当前工作目录，即进入指定目录语法: cd [dirName]
特殊说明:
**`~`表示用户的home目录**
**`.`表示目前所在的目录**
**`..`表示目前目录位置的上级目录**
举例:
```bash
cd ..
cd ~
cd /usr/local
```
##### cat
文件目录操作命令 cat
作用:用于显示文件内容
语法: cat[-n] fileName
说明:
由1开始对所有输出的行数编号-n:
举例:
cat /etc/profile
查看/etc目录下的profile文件内容

##### more
文件目录操作命令 more
作用:以分页的形式显示文件内容
语法: more fileName
操作说明:
回车键
空格键
向下滚动一行
向下滚动一屏
返回上一屏
g或者Ctrl+C退出more
举例:
more /etc/profile
以分页方式显示/etc目录下的profile文件内容

##### tail
文件目录操作命令 tail
作用:查看文件末尾的内容
语法: tail[-f] fileName
说明:
**-f: 动态读取文件末尾内容并显示，通常用于日志文件的内容输出**
举例:
`tail /etc/profile`: 显示/etc目录下的profile文件末尾10行的内容
`tail -20 /etc/profile`: 显示/etc目录下的profile文件末尾20行的内容
`tail -f /itcast/my.log`: 动态读取/itcast目录下的my.log文件未尾内容并显示

##### mkdir
文件目录操作命令 mkdir
作用:创建目录
语法: `mkdir [-p] dirName`
说明:
**-p: 确保目录名称存在，不存在的就创建一个。通过此选项，可以实现多层目录同时创建**
举例:
mkdir itcast 在当前目录下，建立一个名为itcast的子目录

mkdir-p itcast/test 在工作目录下的itcast目录中建立一个名为test的子目录，若itcast目录不存在，则建立一个

##### rmdir
文件目录操作命令 rmdir
作用:删除空目录
语法: `rmdir [-p] dirName`
说明:
-p:当子目录被删除后使父目录为空目录的话，则一并删除
举例:
`rmdir itcast` 删除名为itcast的空目录

`rmdir -p itcast/test` 删除itcast目录中名为test的子目录，若test目录删除后itcast目录变为空目录，则也被删除

`rmdir itcast*` 删除名称以itcast开始的空目录

##### rm
文件目录操作命令 rm
作用:删除文件或者目录
语法: `rm[-rf] name`
说明:
-r: 将目录及目录中所有文件 (目录)逐一删除，即递归删除 
-f: 无需确认，直接删除 

举例:
rm -r itcast/ 删除名为itcast的目录和目录中所有文件，删除前需确认
rm -rf itcast/ 无需确认，直接删除名为itcast的目录和目录中所有文件
rm -f hello.txt 无需确认，直接删除hello.txt文件

#### 4-2. 拷贝移动命令
##### cp
拷贝移动命令 cp
作用:用于复制文件或目录
语法: `cp[-r] source dest`
说明:
-r: 如果复制的是目录需要使用此选项，此时将复制该目录下所有的子目录和文件
举例:
`cp hello.txt itcast/` 将hello.txt复制到itcast目录中
`cp hello.txt ./hi.txt` 将hello.txt复制到当前目录，并改名为hi.txt
`cp -r itcast/ ./itheima/` 将itcast目录和目录下所有文件复制到itheima目录下 
`cp -ritcast/*./itheima/` 将itcast目录下所有文件复制到itheima目录下

##### mv
拷贝移动命令 mv
作用:为文件或目录改名、或将文件或目录移动到其它位置
语法: `mv source dest`
举例:
`mv hello.txt hi.txt`: 将hello.txt改名为hi.txt

`mv hi.txt itheima/`: 将文件hi.txt移动到itheima目录中

`mv hi.txt itheima/hello.txt`: 将hi.txt移动到itheima目录中，并改名为hello.txt

`mv itcast/ itheima/`: 如果itheima目录不存在，将itcast目录改名为itheima
cd ..
`mv itcast/ itheima/`: 如果itheima目录存在，将itcast目录移动到itheima目录中
