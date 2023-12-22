package com.rain.reggie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class Reggie001Application {

    public static void main(String[] args) {
        SpringApplication.run(Reggie001Application.class, args);
    }

}
