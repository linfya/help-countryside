package com.eng;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 助农商城后端启动类。
 */
@EnableScheduling
@MapperScan("com.eng.mapper")
@SpringBootApplication
public class HelpCountrysideApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelpCountrysideApplication.class, args);
    }

}
