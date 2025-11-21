# 가장 쉽고 직관적인 주식 예측, Articker

## 프로젝트 소개

🌐 웹사이트: https://articker.kr

**Articker**는 Article(뉴스)과 Ticker(종목)의 합성어로, 실시간으로 뉴스를 분석하여 주식 종목의 등락 여부를 예측해 주는 서비스입니다.

### 목적

현재 수많은 투자자들이 주식 시장의 실시간 지표인 뉴스를 참고하여 투자 결정을 내리고 있습니다.

Articker는 이러한 투자자들의 행동 패턴에 착안하여, 복잡한 시장 분석에 어려움을 겪는 주식 초보자들을 위한 프로젝트를 기획했습니다.

따라서 뉴스 수집부터 분석하여 예측하는 전 과정을 자동화함으로써 직관적인 투자 보조 지표를 제공하는 데에 중점을 두고 개발되었습니다.

- Articker는 불가능에 가까운 **정확한 가격**을 맞히려는 접근에서 벗어나, 투자자가 뉴스를 보고 ‘오를까 내릴까’를 **판단하는 과정**에 집중합니다.
- 뉴스의 **긍정/부정** **뉘앙스를 분석하여** **주가 등락 방향성**을 제시합니다.

<img width="4403" height="1378" alt="Group 67" src="https://github.com/user-attachments/assets/6875b29d-a45c-465a-a765-1a68cb73f260" />


## 주요 기능

📍 **실시간 주가 예측**

최신 뉴스를 분석하여 종목별 주가의 방향을 예측하고, 매수/매도 전략을 제시합니다.

📍 **주가 그래프**

종목의 가격 변화를 그래프로 한눈에 확인하고, 주가와 관련 뉴스와의 연관성을 비교 분석할 수 있습니다.

📍 **뉴스보드**

실시간으로 다양한 뉴스들을 읽어볼 수 있습니다. 각 기사의 긍정/부정 감정 분석을 통해 특정 종목에 미칠 영향도를 확인할 수 있습니다.

## 개발 기간

- 2025.07.09 ~
    - 2025.09.15: **v1.0.0** 배포

## 프로젝트 멤버

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<table>
  <tr>
    <td align="center">
      <b>Frontend</b>
    </td>
    <td align="center">
      <b>Backend</b>
    </td>
  </tr>
  <tr>
    <td align="center">
      <a href="https://github.com/userjmmm""><img src="https://avatars.githubusercontent.com/u/141299582?v=4" width="80px;" alt=""/><br /><sub><b>이정민</b></sub></a>
    </td>
    <td align="center">
      <a href="https://github.com/anaconda77"><img src="https://avatars.githubusercontent.com/u/62774721?v=4" width="80px;" alt=""/><br /><sub><b>신성민</b></sub></a>
    </td>
  </tr>
</table>
<!-- ALL-CONTRIBUTORS-LIST:END -->

## 기술 스택

- **Front-end**

    ![React](https://img.shields.io/badge/React-61DAFB?style=flat-square&logo=react&logoColor=black)
    ![Vite](https://img.shields.io/badge/Vite-646CFF?style=flat-square&logo=vite&logoColor=white)
    ![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=flat-square&logo=typescript&logoColor=white)
    ![TanStack Query](https://img.shields.io/badge/TanStack%20Query-FF4154?style=flat-square&logo=react-query&logoColor=white)
    ![Axios](https://img.shields.io/badge/Axios-5A29E4?style=flat-square&logo=axios&logoColor=white)
- **Back-end**

    ![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
    ![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
    ![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
    ![AmazonDynamoDB](https://img.shields.io/badge/Amazon%20DynamoDB-4053D6?style=for-the-badge&logo=Amazon%20DynamoDB&logoColor=white)
    ![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)

- **Infra**

    ![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
    ![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
- **Monitoring**
  
    ![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=Prometheus&logoColor=white)
    ![Grafana](https://img.shields.io/badge/grafana-%23F46800.svg?style=for-the-badge&logo=grafana&logoColor=white)
    
- **CI/CD**
  
    ![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white)

## 아키텍처
### 서비스 아키텍처
<img width="2048" height="1182" alt="image" src="https://github.com/user-attachments/assets/df175c29-c0fc-4ae2-b630-4de6d25a54b4" />

### 데이터 수집 아키텍처
<img width="3568" height="2404" alt="image" src="https://github.com/user-attachments/assets/c5b3bbce-302c-444d-b788-865d45d45387" />

### CI/CD 아키텍처
<img width="2048" height="1339" alt="image" src="https://github.com/user-attachments/assets/96a2e47a-9a47-4f7f-9bf3-1d42b567b62d" />


## 모듈 구조

### 📁 module-app

- **애플리케이션의 진입점**

### 📁 module-api

- **API 명세와 유스케이스 단위의 비즈니스 흐름을 조정**
- 외부 요청을 받아 비즈니스 로직을 호출하고, 그 결과를 응답으로 반환하는 컨트롤러를 포함합니다.

### 📁 module-domain

- **순수한 도메인 모델과 핵심 비즈니스 로직**을 포함
- 다른 모듈에 대한 의존성 없이, 오직 비즈니스 규칙에만 집중

### 📁 module-persistence

- **데이터베이스 연동 및 캐시 등 데이터 영속화**를 처리
- CRUD(Create, Read, Update, Delete) 작업 수행

### 📁 module-external

- **외부 API 호출이나 외부 패키지와의 연동**
- 외부 시스템과의 통신에 필요한 클라이언트 및 관련 로직 포함

### 📁 module-common

- **모든 모듈에서 공통으로 참조**하는 코드를 포함
- 예외 처리, 공통 응답 포맷, 유틸리티 클래스 등 특정 비즈니스 로직에 의존하지 않는 코드를 관리


