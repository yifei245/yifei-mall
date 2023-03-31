
package com.yifei.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
* @author wangfei
* @description : 
* @date : 2022/11/18 15:06
*/
@MapperScan("com.yifei.mall.dao")
@SpringBootApplication
public class YifeiMallPlusApplication {
    public static void main(String[] args) {
        SpringApplication.run(YifeiMallPlusApplication.class, args);
    }
}
