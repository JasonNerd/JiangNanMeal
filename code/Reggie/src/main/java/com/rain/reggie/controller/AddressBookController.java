package com.rain.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rain.reggie.common.BaseContext;
import com.rain.reggie.common.R;
import com.rain.reggie.entity.AddressBook;
import com.rain.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("list")
    public R<List<AddressBook>> list(){
        Long userId = BaseContext.getId();
        log.info("查询当前用户所有的地址信息: {}", userId);
        List<AddressBook> books = addressBookService.listByUser(userId);
        return R.success(books);
    }

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

    @GetMapping("{addrId}")
    public R<AddressBook> getById(@PathVariable Long addrId){
        log.info("获取地址: {}", addrId);
        return R.success(addressBookService.getById(addrId));
    }

    @PutMapping
    public R<String> update(@RequestBody AddressBook book){
        log.info("修改地址: {}", book);
        addressBookService.updateById(book);
        return R.success("修改成功");
    }

}
