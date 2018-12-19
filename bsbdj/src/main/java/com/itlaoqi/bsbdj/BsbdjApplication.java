package com.itlaoqi.bsbdj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.itlaoqi.bsbdj.mapper")
@ComponentScan("com.itlaoqi.bsbdj")
public class BsbdjApplication {

	public static void main(String[] args) {
		
		SpringApplication.run(BsbdjApplication.class, args);
		
	}
	
}
