---
title: "Day09-Reggie-用户端开发（二）"
date: 2024-01-11T14:28:40+08:00
draft: false
tags: ["Reggie", "SpringBoot", "JavaWeb"]
categories: ["Reggie"]
twemoji: true
lightgallery: true
---

本章节主要完成地址簿的管理以及订单管理, 同时完善系统的部分细节, Reggie TakeAway 系统篇章基础篇告一段落.

### 1. 地址簿管理
地址簿记录了每个用户的地址信息, 每个用户可以设置多个地址, 但默认地址有且仅有一个.
实体类如下:
```java
@Data
public class AddressBook implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;

    private Long userId;            //用户id
    private String consignee;       //收货人
    private String phone;           //手机号
    private String sex;             //性别 0 女 1 男
    private String provinceCode;    //省级区划编号
    private String provinceName;    //省级名称
    private String cityCode;        //市级区划编号
    private String cityName;        //市级名称
    private String districtCode;    //区级区划编号
    private String districtName;    //区级名称
    private String detail;          //详细地址
    private String label;           //标签
    private Integer isDefault;      //是否默认 0 否 1是

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    private Integer isDeleted;      //是否删除
}
```
为他建立对应的 mapper 和 service 层.

#### 1-1. 新增地址
POST
```java
@RestController
@RequestMapping("addressBook")
@Slf4j
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    @PostMapping
    public R<String> add(@RequestBody AddressBook address){
        log.info("新增地址: {}", address);
        Long userId = BaseContext.getId();
        address.setUserId(userId);
        address.setIsDefault(0);
        addressBookService.save(address);
        return R.success("地址新增成功");
    }
}
```
#### 1-2 查询当前用户的地址簿
```java
@GetMapping("list")
public R<List<AddressBook>> list(){
    Long userId = BaseContext.getId();
    log.info("查询当前用户所有的地址信息: {}", userId);
    List<AddressBook> books = addressBookService.listByUser(userId);
    return R.success(books);
}

// AddressBookServiceImpl
@Override
public List<AddressBook> listByUser(Long userId) {
    LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(AddressBook::getUserId, userId);
    wrapper.orderByDesc(AddressBook::getUpdateTime);
    return this.list(wrapper);
}
```

#### 1-3. 更新默认地址
设置默认地址, 注意需要先把原默认地址取消, 这里有一个 trick, 就是如果一个实体类的字段为空, 那么在更新时该字段不会被写入到数据库中.
```java
@PutMapping("default")
public R<String> defAddr(@RequestBody AddressBook newBook){
    log.info("设置默认地址");
    // 假如已经存在一个默认地址, 则先把它设为0
    LambdaQueryWrapper<AddressBook> bookWrapper = new LambdaQueryWrapper<>();
    bookWrapper.eq(AddressBook::getIsDefault, 1);
    AddressBook bookNoDefault = new AddressBook();
    bookNoDefault.setIsDefault(0);
    addressBookService.update(bookNoDefault, bookWrapper);
    newBook.setIsDefault(1);
    addressBookService.updateById(newBook);
    return R.success("设置默认地址成功");
}
```
#### 1-4. 获取默认地址
```java
@GetMapping("default")
public R<AddressBook> getDefAddr(){
    log.info("获取默认地址");
    LambdaQueryWrapper<AddressBook> bookWrapper = new LambdaQueryWrapper<>();
    bookWrapper.eq(AddressBook::getIsDefault, 1);
    bookWrapper.eq(AddressBook::getUserId, BaseContext.getId());
    AddressBook addressBook = addressBookService.getOne(bookWrapper);
    if (addressBook == null)
        return null;
    else
        return R.success(addressBook);
}
```
#### 1-5. 修改地址
地址信息回显:
```java
@GetMapping("{addrId}")
public R<AddressBook> getById(@PathVariable Long addrId){
    log.info("获取地址: {}", addrId);
    return R.success(addressBookService.getById(addrId));
}
```
更新表格:
```java
@PutMapping
public R<String> update(@RequestBody AddressBook book){
    log.info("修改地址: {}", book);
    addressBookService.updateById(book);
    return R.success("修改成功");
}
```

### 2. 订单管理
#### 2-1. 表结构
Orders 和 OrderDetail
第一个存放订单的基本信息, 包括 订单号 订单所属用户 订单配送地址 等等
```java
@Data
public class Orders implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;

    private String number;                  // 订单号
    private Integer status;                 // 订单状态 1待付款，2待派送，3已派送，4已完成，5已取消
    private Long userId;                    // 下单用户id
    private Long addressBookId;             // 地址id
    private LocalDateTime orderTime;        // 下单时间
    private LocalDateTime checkoutTime;     // 结账时间
    private Integer payMethod;              // 支付方式 1 微信, 2 支付宝
    private BigDecimal amount;              // 实收金额
    private String remark;                  // 备注
    private String userName;                // 用户名
    private String phone;                   // 手机号
    private String address;                 // 地址
    private String consignee;               // 收货人
}
```

第二个是订单明细表, 它以订单号为关联键值, 存放该订单中包含的所有菜品或套餐的信息
```java
@Data
public class OrderDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;

    private String name;    //名称
    private Long orderId;   //订单id
    private Long dishId;    //菜品id
    private Long setmealId; //套餐id
    private String dishFlavor;      //口味
    private Integer number;         //数量
    private BigDecimal amount;      //金额
    private String image;           //图片
}
```
为他们创建 service 和 mapper.

#### 2-2 提交订单
如下为请求信息:
```
请求 URL:
http://localhost:8080/order/submit
请求方法:
POST
请求体:
{
    "remark": "",
    "payMethod": 1,
    "addressBookId": "1746045643976040449"
}
```
订单生成的步骤包括:
1. 获取当前登录用户
2. 获取用户地址信息
3. 获取购物车菜品信息
4. 创建订单实体
5. 保存订单明细表到数据库
6. 清空购物车
7. 填充订单实体的必要字段, 保存订单信息到数据库

如下为 controller 层的处理方法:
```java
@RestController
@RequestMapping("order")
@Slf4j
public class OrderController {
    @Autowired
    private OrdersService ordersService;

    @PostMapping("submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("用户下单: {}", orders);
        ordersService.submit(orders);
        return R.success("下单成功");
    }
}
```

OrdersServiceImpl.submit 方法如下:
```java
@Override
public void submit(Orders orders) {
    Long userId = BaseContext.getId();
    User user = userService.getById(userId);

    Long addressBookId = orders.getAddressBookId();
    AddressBook addressBook = addressBookService.getById(addressBookId);
    if (addressBook == null)
        throw new BusinessException("地址信息有误, 无法下单");

    Long orderId = IdWorker.getId();
    orders.setId(orderId);
    int amount = orderDetailService.saveCurrentShoppingCart(orderId);
    orders.setOrderTime(LocalDateTime.now());
    orders.setCheckoutTime(LocalDateTime.now());
    orders.setStatus(2);
    orders.setAmount(new BigDecimal(amount));
    orders.setUserId(userId);
    orders.setNumber(String.valueOf(orderId));
    orders.setUserName(user.getName());
    orders.setConsignee(addressBook.getConsignee());
    orders.setPhone(addressBook.getPhone());
    orders.setAddress(
            (addressBook.getProvinceName() == null ? "": addressBook.getProvinceName()) +
                    (addressBook.getCityName() == null ? "": addressBook.getCityName()) +
                    (addressBook.getDistrictName() == null ? "": addressBook.getDistrictName()) +
                    (addressBook.getDetail() == null ? "": addressBook.getDetail())
    );
    this.save(orders);
}
```
保存订单明细表到数据库的代码如下:
```java
@Transactional
@Override
public Integer saveCurrentShoppingCart(Long orderId) {
    // 将当前用户购物车内的套餐或菜品全部存入订单明细表中, 并与订单号 orderId 关联
    // 返回购物车内商品总金额 amount, 同时清除购物车.
    AtomicInteger amount = new AtomicInteger(0);
    Long userId = BaseContext.getId();

    LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ShoppingCart::getUserId, userId);
    List<ShoppingCart> shoppingCarts = shoppingCartService.list(wrapper);
    if (shoppingCarts==null || shoppingCarts.size()==0)
        throw new BusinessException("购物车为空, 无法下单");

    for (ShoppingCart cart: shoppingCarts){
        OrderDetail detail = new OrderDetail();
        BeanUtils.copyProperties(cart, detail);
        detail.setOrderId(orderId);
        amount.addAndGet(detail.getAmount().multiply(new BigDecimal(detail.getNumber())).intValue());
        this.save(detail);
    }

    shoppingCartService.remove(wrapper);
    return amount.get();
}
```

实际上还有一些请求待处理, 例如点击套餐图片展示套餐明细、点击历史订单展示订单列表及其明细等等, 由于请求路径和方法以及返回数据格式等未知, 且未新增技术点, 故暂时不进行讨论. 至此, Reggie 外卖项目的基本内容就完结了。


