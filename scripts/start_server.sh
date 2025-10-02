#!/bin/bash
set -e # 오류 발생 시 즉시 중단

# 작업 디렉토리로 이동
cd /home/ubuntu/app

# 1. S3에서 환경변수 파일 로드
ENV_FILE=".env-prod"
trap 'rm -f "$ENV_FILE"' EXIT
aws s3 cp s3://articker-codedeploy-artifacts-bucket/.env-prod "$ENV_FILE"

# EC2 메타데이터에서 현재 인스턴스의 고유 ID를 가져옵니다.
INSTANCE_ID=$(curl -s http://169.254.169.254/latest/meta-data/instance-id)
# docker-compose가 사용할 수 있도록 환경 변수로 export 합니다.
export INSTANCE_ID

# 2. 배포된 파일에서 실행할 Docker 이미지 이름을 읽어옴
export SPRING_APP_IMAGE=$(cat ./image_name.txt)

# 3. Docker Hub에서 최신 이미지 받아오기
docker pull $SPRING_APP_IMAGE

# 4. docker-compose로 컨테이너 실행 (기존 컨테이너가 있다면 중지 후 재시작)
docker-compose down
docker-compose up -d

# 5. 보안을 위해 사용했던 .env-prod 파일 즉시 삭제
rm -f "$ENV_FILE"

# 6. 사용하지 않는 Docker 이미지 정리 (선택 사항)
docker image prune -a -f