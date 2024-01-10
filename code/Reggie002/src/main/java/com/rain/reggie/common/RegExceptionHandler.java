package com.rain.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

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

    @ExceptionHandler(BusinessException.class)
    public R<String> exceptionHandler(BusinessException e){
        String message = e.getMessage();
        log.error("Exception occurred: {}", message);
        return R.error(message);
    }
}
