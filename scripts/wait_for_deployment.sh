#!/bin/bash
set -e

if [ -z "$1" ]; then
  echo "오류: 배포 ID가 필요합니다."
  exit 1
fi

DEPLOYMENT_ID=$1
echo "➡️ 배포 ID: ${DEPLOYMENT_ID}"
echo "⏳ 배포가 'Ready' 상태가 될 때까지 대기 시작..."

# 최대 15분 동안 15초 간격으로 배포 상태를 확인 (총 60회)
for i in {1..60}; do
  # ⭐️ 배포의 전체 상태('status')만 직접 조회합니다.
  DEPLOYMENT_STATUS=$(aws deploy get-deployment --deployment-id ${DEPLOYMENT_ID} --query "deploymentInfo.status" --output text)

  echo "🔍 현재 배포 상태: ${DEPLOYMENT_STATUS} (시도 $i/60)"

  # ⭐️ 상태가 'Ready'이면 성공으로 간주하고 즉시 종료합니다.
  if [ "$DEPLOYMENT_STATUS" == "Ready" ]; then
    echo "🎉 배포가 'Ready' 상태에 도달했습니다! 다음 단계로 진행합니다."
    exit 0
  fi

  # 배포가 실패하거나 중지되면 즉시 워크플로우를 실패 처리합니다.
  if [ "$DEPLOYMENT_STATUS" == "Failed" ] || [ "$DEPLOYMENT_STATUS" == "Stopped" ]; then
    echo "❌ 배포가 실패했거나 중지되었습니다. 워크플로우를 중단합니다."
    exit 1
  fi

  sleep 15
done

echo "⏰ 타임아웃: 시간 내에 배포가 'Ready' 상태에 도달하지 않았습니다."
exit 1