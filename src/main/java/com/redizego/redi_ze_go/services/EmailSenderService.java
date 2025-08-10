package com.redizego.redi_ze_go.services;

public interface EmailSenderService {

    void sendEmail(String toEmail,String subject,String body);

    void sendEmail(String toEmail[],String subject,String body);
}
