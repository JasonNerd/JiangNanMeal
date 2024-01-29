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
