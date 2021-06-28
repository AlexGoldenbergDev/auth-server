package com.goldenebrg.authserver.mail;

public interface MailService {
    void sendMessage(String receiver, String subject, String content);
}
