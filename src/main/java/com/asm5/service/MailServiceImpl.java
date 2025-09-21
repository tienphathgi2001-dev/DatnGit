package com.asm5.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class MailServiceImpl implements MailService {
	@Autowired
	JavaMailSender mailSender;

	public void send(Mail mail) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");

			helper.setFrom(mail.getFrom());
			helper.setReplyTo(mail.getFrom());
			helper.setTo(mail.getTo());
			if (!this.isNullOrEmpty(mail.getCc())) {
				helper.setCc(mail.getCc());
			}
			if (!this.isNullOrEmpty(mail.getBcc())) {
				helper.setBcc(mail.getBcc());
			}
			helper.setSubject(mail.getSubject());
			helper.setText(mail.getBody(), true);
			// 2.4. Đính kèm file
			String filenames = mail.getFilenames();
			if (!this.isNullOrEmpty(filenames)) {
				for (String filename : filenames.split("[,;]+")) {
					File file = new File(filename.trim());
					helper.addAttachment(file.getName(), file);
				}
			}

			mailSender.send(message);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isNullOrEmpty(String text) {
		return (text == null || text.trim().length() == 0);
	}

	List<Mail> queue = new ArrayList<>();
	@Override
	public void push(Mail mail) {
		queue.add(mail);
	}

	@Scheduled(fixedDelay = 500)
	public void run() {
		while (!queue.isEmpty()) {
			try {
				this.send(queue.remove(0));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
