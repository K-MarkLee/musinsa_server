package com.mudosa.musinsa.notification.service;

import com.mudosa.musinsa.notification.domain.dto.NoteMetaDTO;
import com.mudosa.musinsa.notification.domain.service.NoteMetaService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Log4j2
public class NoteMetaServiceTest {
    @Autowired
    private NoteMetaService noteMetaService;

    @Test
    public void getTest(){
        NoteMetaDTO noteMetaDTO = noteMetaService.get("RESTOCK");
        log.info(noteMetaDTO.toString());
    }
}
