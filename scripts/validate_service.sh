#!/bin/bash
set -e

# 10번 재시도, 5초 간격으로 헬스 체크
for i in {1..10}; do
    # Spring Boot Actuator health check endpoint
    # 응답에 "UP"이 포함되어 있으면 성공
    if response=$(curl -sf http://localhost:8080/actuator/health); then
      if echo "$response" | grep -q '"status":"UP"'; then
        echo "Health check PASSED"
        exit 0
      fi
    fi

    echo "Health check attempt $i failed. Retrying in 5 seconds..."
    sleep 5
done

# 10번 모두 실패하면 배포 실패 처리
echo "Health check FAILED after multiple attempts."
exit 1