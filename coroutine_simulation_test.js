import http from 'k6/http';
import {check, sleep} from 'k6';
import {randomItem} from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

// k6가 보낼 Prediction 작업의 종류 (Blocking/Coroutine)
const BATCH_TARGET = __ENV.BATCH_TARGET || 'blocking';
// Prediction 작업에 Write Lock(InitTask)을 섞을지 여부
const SCENARIO_TYPE = __ENV.SCENARIO || '1'; // '1'=Write Lock 없음, '2'=1% 섞음

export const options = {
    thresholds: {
        // [매우 중요] 테스트의 성공 여부는 오직 '일반 유저 API'의 성능으로만 판단합니다.
        'http_req_failed{api:normal_users}': ['rate<0.01'], // '뉴스 보기' 실패율 1% 미만
        'http_req_duration{api:normal_users}': ['p(95)<500'], // '뉴스 보기' 95% 응답속도 500ms 미만
    },
    scenarios: {
        // 시나리오 1: 일반 유저 트래픽 (보호 대상)
        normal_users: {
            executor: 'constant-vus',
            vus: 50, // 50명의 유저가 꾸준히 접속
            duration: '1m10s', // 전체 테스트 시간
            exec: 'runNormalUserTraffic', // 실행할 함수
        },
        // 시나리오 2: Prediction 배치 작업 (부하 유발)
        prediction_batch: {
            executor: 'constant-vus',
            vus: 150,
            duration: '1m',   // 1분간 부하 지속
            startTime: '10s', // 10초 뒤에 부하 시작
            exec: 'runPredictionBatch', // 실행할 함수
        },
    },
};

// [실행 함수 1] 일반 유저 (보호 대상)
// 이 API는 서버의 `MixedTrafficSimulation.kt`에 구현되어 있어야 합니다.
export function runNormalUserTraffic() {
    const res = http.get('http://localhost:8080/test/normal', {
        timeout: '1s', // 1초 응답 보장
        tags: { api: 'normal_users' }, // Thresholds와 연결하기 위한 태그
    });

    check(res, {
        'News API is OK (200)': (r) => r.status === 200,
    }, { api: 'normal_users' });

    // 유저는 1초에 한 번씩 뉴스 API를 봅니다.
    sleep(1);
}

// [실행 함수 2] Prediction 배치 (부하 유발)
export function runPredictionBatch() {
    let body;
    if (SCENARIO_TYPE === '2') {
        // Init 비율을 1%로 설정(주요 병목 요인)
        const isInitTask = Math.random() < 0.01;
        body = isInitTask ? createInitPayload() : createArticlePayload();
    } else {
        body = createArticlePayload();
    }

    const params = {
        headers: { 'Content-Type': 'application/json' },
        timeout: '3s',
        tags: { api: 'prediction_batch' },
    };

    // BATCH_TARGET (blocking / coroutine)에 따라 다른 API 호출
    http.post(`http://localhost:8080/test/${BATCH_TARGET}`, body, params);

    // SQS 리스너처럼, 작업이 끝나자마자 다음 작업을 가져옴 (sleep 없음)
}

// --- Payload 생성 헬퍼 ---
const WILDCARD_UUID = "00000000-0000-0000-0000-000000000000";
const TICKER_IDS = [
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000001",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000002",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000003",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000004",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000005",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000006",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000007",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000008",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000009",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000010",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000011",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000012",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000013",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000014",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000015",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000016",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000017",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000018",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000019",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000020",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000021",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000022",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000023",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000024",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000025",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000026",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000027",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000028",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000029",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000030",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000031",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000032",
    "aaaaaaaa-aaaa-aaaa-aaaa-000000000033"
];

function createArticlePayload() {
    return JSON.stringify({
        type: "article",
        tickerId: randomItem(TICKER_IDS),
        payload: {
            predictionDate: new Date().toISOString(),
            positiveArticleCount: Math.floor(Math.random() * 10),
            negativeArticleCount: Math.floor(Math.random() * 5),
            neutralArticleCount: Math.floor(Math.random() * 5),
            createdAt: new Date().toISOString()
        }
    });
}

function createInitPayload() {
    return JSON.stringify({
        type: "init",
        tickerId: WILDCARD_UUID,
        payload: {
            tickerCode: "005930",
            shortCompanyName: "SamsungElec",
            predictionDate: new Date().toISOString(),
            todayMacd: { "macd": 0.55, "signal": 0.32 },
            yesterdayMacd: { "macd": 0.45, "signal": 0.28 },
            todayMa: { "ma5": 60100.0, "ma20": 59200.0 },
            yesterdayMa: { "ma5": 59800.0, "ma20": 58900.0 },
            todayRsi: 65.5,
            todayHigh: 61500.0,
            todayLow: 59000.0,
            yesterdayClose: 60000.0,
            createdAt: new Date().toISOString()
        }
    });
}