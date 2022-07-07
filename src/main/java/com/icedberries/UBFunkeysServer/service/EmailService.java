package com.icedberries.UBFunkeysServer.service;

public interface EmailService {

    Boolean sendMailWithAttachment(String to, String subject, String body, String fileToAttach);

}
