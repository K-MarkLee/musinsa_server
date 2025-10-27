#!/bin/bash

# 결제 승인 테스트 실행 스크립트

echo "🧪 결제 승인 테스트 시작"
echo "================================"

# 1. 모든 단위 테스트 실행
echo ""
echo "📝 1. 단위 테스트 실행 중..."
./gradlew test --tests "com.mudosa.musinsa.payment.application.service.*Test" \
  --exclude-test "*IntegrationTest" \
  --console=plain

if [ $? -eq 0 ]; then
    echo "✅ 단위 테스트 통과"
else
    echo "❌ 단위 테스트 실패"
    exit 1
fi

# 2. 통합 테스트 실행 (선택적)
echo ""
echo "📝 2. 통합 테스트 실행 중..."
./gradlew test --tests "*IntegrationTest" --console=plain

if [ $? -eq 0 ]; then
    echo "✅ 통합 테스트 통과"
else
    echo "⚠️  통합 테스트 실패 (실제 API 호출 필요)"
fi

# 3. 테스트 리포트 생성
echo ""
echo "📊 테스트 리포트 생성 중..."
./gradlew jacocoTestReport

echo ""
echo "================================"
echo "✨ 테스트 완료!"
echo "📄 리포트 위치: build/reports/tests/test/index.html"
echo "📊 커버리지: build/reports/jacoco/test/html/index.html"
