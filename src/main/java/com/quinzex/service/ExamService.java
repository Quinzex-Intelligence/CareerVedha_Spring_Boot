package com.quinzex.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;



@Service
public class EmailService implements IemailService {

    private final IotpService iotpService;
    private final SpringTemplateEngine templateEngine;
    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(IotpService iotpService,JavaMailSender javaMailSender,SpringTemplateEngine templateEngine){
        this.iotpService=iotpService;
        this.javaMailSender=javaMailSender;
        this.templateEngine=templateEngine;
    }

    @Async
    @Override
    public void sendEmail(String email,String otp) {


        Context context = new Context();
        context.setVariable("otp", otp);
        try{
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message,true,"UTF-8");
            messageHelper.setFrom(fromEmail);
            messageHelper.setTo(email);
            messageHelper.setSubject("Verify your Career Vedha Registration");
            messageHelper.setText(
                    templateEngine.process("otp-register", context),
                    true
            );

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("OTP email sending failed", e);
        }
    }
    @Async
    @Override
    public void sendLoginEmail(String email) {
        String otp =    iotpService.generateLoginOtp(email);
        Context context = new Context();
        context.setVariable("otp", otp);
        try{
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message,true,"UTF-8");
            messageHelper.setFrom(fromEmail);
            messageHelper.setTo(email);
            messageHelper.setSubject("Verify your Career Vedha login");
            messageHelper.setText(
                    templateEngine.process("otp-verify", context),
                    true
            );

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("OTP email sending failed", e);
        }
    }
    @Async
    @Override
    public void sendSuspeciousEmail(String email) {

        try{
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Suspicious Login Alert – Career Vedha");
            Context context = new Context();
            String htmlContent = templateEngine.process("suspicious-login",context);
            helper.setText(htmlContent,true);
            javaMailSender.send(message);
        }catch (MessagingException e){
            throw new RuntimeException("Email Sending failed",e);
        }

    }

    @Async
    @Override
    public void sendApprovalEmail(String email, String role) {

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Role Approved – Career Vedha");
            Context context = new Context();
            context.setVariable("role",role);
            String htmlContent = templateEngine.process("role-approved",context);
            helper.setText(htmlContent,true);
            javaMailSender.send(message);
        }catch (MessagingException  e){
            throw new RuntimeException("Email Sending Failed ",e);
        }
    }
    @Async
    @Override
    public void sendRejectionEmail(String email, String role, String reason) {
        try{
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Role Request Rejected – Career Vedha");
            Context context = new Context();
            context.setVariable("role",role);
            context.setVariable(
                    "reason",
                    reason != null ? reason : "Not specified"
            );
            String htmlContent= templateEngine.process("role-rejected",context);
            helper.setText(htmlContent,true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Email Sending Failed",e);
        }
    }

    @Async
    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent,true);
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}