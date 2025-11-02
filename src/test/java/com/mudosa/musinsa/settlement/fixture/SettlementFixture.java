package com.mudosa.musinsa.settlement.fixture;

import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.settlement.domain.model.SettlementDaily;
import com.mudosa.musinsa.settlement.domain.model.SettlementPerTransaction;
import com.mudosa.musinsa.settlement.domain.model.TransactionType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Settlement 테스트용 픽스처 (테스트 데이터 생성 헬퍼)
 * 프로덕션 코드를 오염시키지 않고 테스트 데이터를 생성합니다.
 */
public class SettlementFixture {

    /**
     * 테스트용 거래별 정산 생성 (리플렉션 사용)
     */
    public static SettlementPerTransaction createTransaction(
        Long brandId,
        Long orderId,
        LocalDate settlementDate,
        String transactionAmount,
        String commissionAmount,
        String taxAmount,
        String pgFeeAmount
    ) {
        try {
            // protected 생성자 접근
            Constructor<SettlementPerTransaction> constructor =
                SettlementPerTransaction.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            SettlementPerTransaction transaction = constructor.newInstance();

            // 필드 설정
            setField(transaction, "brandId", brandId);
            setField(transaction, "paymentId", orderId);
            setField(transaction, "pgTransactionId", "TEST-" + orderId);
            setField(transaction, "transactionDateLocal", settlementDate);
            setField(transaction, "transactionDate", settlementDate.atStartOfDay());
            setField(transaction, "transactionType", TransactionType.ORDER);
            setField(transaction, "timezoneOffset", "Asia/Seoul");
            setField(transaction, "commissionRate", BigDecimal.ZERO);

            setField(transaction, "transactionAmount", new Money(new BigDecimal(transactionAmount)));
            setField(transaction, "commissionAmount", new Money(new BigDecimal(commissionAmount)));
            setField(transaction, "taxAmount", new Money(new BigDecimal(taxAmount)));
            setField(transaction, "pgFeeAmount", new Money(new BigDecimal(pgFeeAmount)));

            return transaction;
        } catch (Exception e) {
            throw new RuntimeException("테스트 데이터 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 테스트용 일일 정산 생성
     */
    public static SettlementDaily createDailySettlement(
        Long brandId,
        LocalDate settlementDate,
        int orderCount,
        String salesAmount,
        String commissionAmount,
        String taxAmount,
        String pgFeeAmount
    ) {
        SettlementDaily daily = SettlementDaily.create(
            brandId,
            settlementDate,
            "DAILY-TEST-" + settlementDate,
            "Asia/Seoul"
        );

        daily.setAggregatedData(
            orderCount,
            new Money(new BigDecimal(salesAmount)),
            new Money(new BigDecimal(commissionAmount)),
            new Money(new BigDecimal(taxAmount)),
            new Money(new BigDecimal(pgFeeAmount))
        );

        daily.startProcessing();
        daily.complete();

        return daily;
    }

    /**
     * 리플렉션으로 private 필드 설정 (테스트 전용)
     */
    private static void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
