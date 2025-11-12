package com.mudosa.musinsa.domain.chat.repository;

import com.mudosa.musinsa.brand.domain.repository.BrandRepository;
import com.mudosa.musinsa.settlement.domain.repository.SettlementDailyMapper;
import com.mudosa.musinsa.settlement.domain.repository.SettlementMonthlyMapper;
import com.mudosa.musinsa.settlement.domain.repository.SettlementPerTransactionMapper;
import com.mudosa.musinsa.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

// 공통 테스트 설정 분리
@Slf4j
@ActiveProfiles("test")
@DataJpaTest
@ImportAutoConfiguration(exclude = MybatisAutoConfiguration.class)
@Import(JpaAuditTestConfig.class)
public abstract class JpaConfig {
  @Autowired
  protected ChatRoomRepository chatRoomRepository;
  @Autowired
  protected ChatPartRepository chatPartRepository;
  @Autowired
  protected UserRepository userRepository;
  @Autowired
  protected BrandRepository brandRepository;
  @Autowired
  protected MessageRepository messageRepository;
  @Autowired
  protected MessageAttachmentRepository attachmentRepository;

  /**
   * 오류 발생으로 MockBean 처리
   * 채팅 테스트에서는 사용하지 않으므로...
   *
   */
  @MockitoBean
  private SettlementDailyMapper settlementDailyMapper;
  @MockitoBean
  private SettlementMonthlyMapper settlementMonthlyMapper;
  @MockitoBean
  private SettlementPerTransactionMapper settlementPerTransactionMapper;
}
