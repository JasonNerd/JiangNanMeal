package com.rain.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rain.reggie.entity.AddressBook;

import java.util.List;

public interface AddressBookService extends IService<AddressBook> {
    List<AddressBook> listByUser(Long userId);
}
