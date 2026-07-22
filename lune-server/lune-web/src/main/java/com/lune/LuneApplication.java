package com.lune;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lune.mapper")
public class LuneApplication {
    public static void main(String[] args) {
        SpringApplication.run(LuneApplication.class, args);
    }
}
