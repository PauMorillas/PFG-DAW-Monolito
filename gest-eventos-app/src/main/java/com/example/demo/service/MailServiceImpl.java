package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.demo.model.Mail;

@Service
public class MailServiceImpl implements MailService {
	@Autowired
	private JavaMailSender javaMailSender;
	
	@Value("${spring.mail.username}")
	private String username;

	public void enviarMail(Mail mail) {
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setTo(mail.getTo());
		msg.setSubject(mail.getSubject());
		msg.setText(mail.getText());
		msg.setFrom(username);
		
		javaMailSender.send(msg);
	}
}
