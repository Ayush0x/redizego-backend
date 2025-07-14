package com.redizego.redi_ze_go;

import com.redizego.redi_ze_go.services.EmailSenderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RediZeGoApplicationTests {


	@Autowired
	private EmailSenderService emailSenderService;
	@Test
	void contextLoads() {
		emailSenderService.sendEmail("ayush251299@gmail.com","this is test email","this is the body of test email");
	}

	@Test
	void sendEmailsMultiple(){
		String[] emails={"ayush251299@gmail.com",
				"tf141st@gmail.com",
				"scriptsensei593@gmail.com",
		"rohit.verma.rv.4682@gmail.com"};

		emailSenderService.sendEmail(emails,
				"This is a test mail",
				"This is the body of the email");
	}
}
