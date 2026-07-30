package com.lune;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.lune.mapper")
@EnableAsync
public class LuneApplication {
    public static void main(String[] args) {
        SpringApplication.run(LuneApplication.class, args);
    }
}
