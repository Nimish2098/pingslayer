package com.nimish.pingslayer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PingslayerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PingslayerApplication.class, args);
	}

}
