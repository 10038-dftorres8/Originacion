package com.banquito.originacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OriginacionApplication {

	public static void main(String[] args) {
		SpringApplication.run(OriginacionApplication.class, args);
	}

}
