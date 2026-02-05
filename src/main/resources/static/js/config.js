/**
 * 프로젝트 전역 설정 파일
 * API URL 및 상수 값을 여기서 통합 관리합니다.
 */

const API_BASE = "/api/v1"; // 기본 API 경로

export const API_URLS = {
    // 1. 거래 관련
    TRANSACTION: `${API_BASE}/transactions`,

    // 2. 관리자 기능 (승인/거절/설정)
    ADMIN: `${API_BASE}/admin`,

    // 3. 계좌 조회
    ACCOUNT: `${API_BASE}/accounts`,

    // 4. 사기 탐지 및 신고
    FRAUD: "/api/fraud",    // (컨트롤러 경로에 따라 수정 가능)
    REPORT: "/api/reports"  // 신고 API
};

export const UI_CONSTANTS = {
    // 나중에 UI 관련 설정도 여기서 관리 가능 (예: 자동 새로고침 간격)
    REFRESH_INTERVAL: 5000
};