package com.rain.reggie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@ServletComponentScan
@EnableCaching
public class Reggie002Application {

    public static void main(String[] args) {
        SpringApplication.run(Reggie002Application.class, args);
    }

}
