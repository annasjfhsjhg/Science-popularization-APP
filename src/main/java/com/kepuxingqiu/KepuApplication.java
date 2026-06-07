package com.kepuxingqiu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.kepuxingqiu.mapper")
public class KepuApplication {
    public static void main(String[] args) {
        SpringApplication.run(KepuApplication.class, args);
    }
}
