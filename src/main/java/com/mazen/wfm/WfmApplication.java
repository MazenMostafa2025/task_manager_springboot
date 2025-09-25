package com.mazen.wfm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class WfmApplication {

	public static void main(String[] args) {
		SpringApplication.run(WfmApplication.class, args);
	}

}
