package com.mudosa.musinsa.notification.domain.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class FcmService {

    @Value("${fcm.service-acount-file}")
    private String serviceAcountFilePath;

    @Value("${fcm.topic-name}")
    private String topicName;

    @Value("${fcm.project-id}")
    private String projectId;

    @PostConstruct
    public void initialize() throws IOException {

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(serviceAcountFilePath).getInputStream()))
                .setProjectId(projectId)
                .build();

        FirebaseApp.initializeApp(options);
    }

    public void sendMessageByTopic(String title, String body) throws IOException, FirebaseMessagingException {
        FirebaseMessaging.getInstance().send(Message.builder()
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build())
            .setTopic(topicName)
            .build());
    }

    public void sendMessageByToken(String title, String body, String token) throws FirebaseMessagingException {
        FirebaseMessaging.getInstance().send(Message.builder()
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .setToken(token)
                .build());
    }
}
