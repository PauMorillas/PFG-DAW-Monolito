package com.example.demo.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.model.Mail;
import com.example.demo.service.MailService;

@Controller
@RequestMapping("/mail")
public class MailController {
	@Autowired
	private MailService mailService;

	@PostMapping("/envio")
	@ResponseBody
	public String enviarMail(@RequestBody Mail mail) {
		mailService.enviarMail(mail);

		return "El mail ha sido enviado";
	}
}
