#!/bin/bash
set -e

PROJECT_NAME="articker-app"

# 작업 디렉토리로 이동
cd /home/ubuntu/app

# docker-compose.yml 파일이 존재하면 컨테이너를 중지하고 네트워크 등을 제거
if [ -f "docker-compose.yml" ]; then
    sudo -E docker compose -p $PROJECT_NAME down
fi