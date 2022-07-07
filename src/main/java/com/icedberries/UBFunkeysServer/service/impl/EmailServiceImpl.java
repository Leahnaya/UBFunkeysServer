package com.icedberries.UBFunkeysServer.service.impl;

import com.icedberries.UBFunkeysServer.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Service("emailService")
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailsender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public Boolean sendMailWithAttachment(String to, String subject, String body, String fileToAttach) {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                // Set the main parts of the email
                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
                mimeMessage.setFrom(new InternetAddress(from));
                mimeMessage.setSubject(subject);
                mimeMessage.setText(body);

                // Attach the postcard
                Resource resource = new ClassPathResource("static/Postcards/" + fileToAttach);
                byte[] fileContent = org.apache.commons.io.IOUtils.toByteArray(resource.getInputStream());
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.addAttachment(fileToAttach, new ByteArrayResource(fileContent));
            }
        };

        try {
            mailsender.send(preparator);
            System.out.println("[GALAXY][POST] Email send successfully to: " + to);
            return true;
        } catch(MailException e) {
            System.out.println("[GALAXY][POST][ERROR] Unable to send email to: " + to);
            e.printStackTrace();
            return false;
        }
    }
}
