package com.mudosa.musinsa.notification.service;

import com.mudosa.musinsa.notification.domain.dto.NotificationDTO;
import com.mudosa.musinsa.notification.domain.service.NotificationService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Log4j2
public class NotificationServiceTest {
    @Autowired
    private NotificationService notificationService;

    @Test
    public void notificationReadTest(){
        List<NotificationDTO> listDto = notificationService.get(1L);
        log.info("listDto={}", listDto);
    }
}
