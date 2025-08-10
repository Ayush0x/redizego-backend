package com.redizego.redi_ze_go;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRetry
@EnableScheduling
public class RediZeGoApplication {

	public static void main(String[] args) {
		SpringApplication.run(RediZeGoApplication.class, args);

	}

}
