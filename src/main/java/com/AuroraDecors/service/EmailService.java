package com.AuroraDecors.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    public void sendOTP(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset OTP - Tab on Furniture");
        message.setText("Your OTP for password reset is: " + otp + "\n\n" +
                       "This OTP is valid for 10 minutes.\n" +
                       "If you didn't request this, please ignore this email.\n\n" +
                       "Best regards,\nTab on Furniture Team");
        
        mailSender.send(message);
    }
    
    public void sendPasswordResetSuccess(String to, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset Successful - Tab on Furniture");
        message.setText("Dear " + username + ",\n\n" +
                       "Your password has been successfully reset.\n" +
                       "You can now login with your new password.\n\n" +
                       "If you didn't perform this action, please contact our support team immediately.\n\n" +
                       "Best regards,\nTab on Furniture Team");
        
        mailSender.send(message);
    }
    
    public void sendLoginNotification(String to, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("New Login Detected - Tab on Furniture");
        message.setText("Dear " + username + ",\n\n" +
                       "Your account was just used to sign in to Tab on Furniture.\n" +
                       "If this was you, no action is needed.\n\n" +
                       "If you didn't perform this login, please reset your password immediately and contact support.\n\n" +
                       "Best regards,\nTab on Furniture Team");
        mailSender.send(message);
    }
} 