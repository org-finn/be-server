import http from 'k6/http';
import { check } from 'k6';
import { randomItem } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

// ===========================================================================
// 1. 테스트 설정
// ===========================================================================
export const options = {
    scenarios: {
        default_load: {
            executor: 'constant-vus',
            vus: 100,              // 동시 처리 스레드/코루틴 수
            duration: '30s',       // 30초간 지속
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'], 
        http_req_duration: ['p(95)<5000'], // 락 대기로 인해 최대 5초 허용
    },
};

// ===========================================================================
// 2. 설정 값
// ===========================================================================
const BASE_URL = 'http://localhost:8080/test'; 
// 실행 시 옵션: k6 run -e TARGET=coroutine -e SCENARIO=2 script.js
const TARGET_TYPE = __ENV.TARGET || 'blocking'; 
const SCENARIO_TYPE = __ENV.SCENARIO || '1';    

const WILDCARD_UUID = "00000000-0000-0000-0000-000000000000";

const TICKER_IDS = [
    "11111111-1111-1111-1111-111111111111",
    "22222222-2222-2222-2222-222222222222",
    "33333333-3333-3333-3333-333333333333",
    "44444444-4444-4444-4444-444444444444",
    "55555555-5555-5555-5555-555555555555"
];

// ===========================================================================
// 3. Payload 생성 팩토리 (수정됨)
// ===========================================================================

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
        tickerId: WILDCARD_UUID, // Write Lock Trigger
        payload: {
            tickerCode: "005930",
            shortCompanyName: "SamsungElec",
            predictionDate: new Date().toISOString(),
            
            // [수정 완료] 실제 데이터 양식 반영 (macd, signal / ma5, ma20)
            todayMacd: { 
                "macd": 0.55, 
                "signal": 0.32 
            },
            yesterdayMacd: { 
                "macd": 0.45, 
                "signal": 0.28 
            },
            todayMa: { 
                "ma5": 60100.0, 
                "ma20": 59200.0 
            },
            yesterdayMa: { 
                "ma5": 59800.0, 
                "ma20": 58900.0 
            },
            
            todayRsi: 65.5,
            todayHigh: 61500.0,
            todayLow: 59000.0,
            yesterdayClose: 60000.0,
            createdAt: new Date().toISOString()
        }
    });
}

// ===========================================================================
// 4. 메인 실행 함수
// ===========================================================================
export default function () {
    const url = `${BASE_URL}/${TARGET_TYPE}`;
    const params = {
        headers: { 'Content-Type': 'application/json' },
        timeout: '15s' // 대기열이 길어질 수 있으므로 타임아웃 넉넉히 설정
    };

    let body;

    if (SCENARIO_TYPE === '2') {
        // [시나리오 2] Mixed: 10% Init(Write Lock) + 90% Article(Read Lock)
        const isInitTask = Math.random() < 0.1; 
        body = isInitTask ? createInitPayload() : createArticlePayload();
    } else {
        // [시나리오 1] Article Only
        body = createArticlePayload();
    }

    const res = http.post(url, body, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}
