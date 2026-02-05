import { FdsApi } from './api.js';
import { UiRenderer } from './ui-renderer.js';
import { UiHandler } from './ui-handler.js';

export const UiMain = {
    // [1] 초기화
    init() {
        console.log("🚀 FDS Dashboard Initializing...");

        // 초기 데이터 로드
        this.refreshAll();

        // 탭 전환 이벤트
        document.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const type = e.target.dataset.section;
                this.showSection(type, e.target);
            });
        });

        // 핸들러(버튼/슬라이더 이벤트) 초기화
        UiHandler.initEventHandlers();
    },

    // [2] 전체 데이터 갱신
    async refreshAll() {
        try {
            // A. 기본 데이터 호출
            const [allHistory, fraudDetails, blacklist] = await Promise.all([
                FdsApi.fetchHistory(),
                FdsApi.fetchFraudOnly(),
                FdsApi.fetchBlacklist()
            ]);

            // B. 카운트 로직
            const totalCount = allHistory ? allHistory.length : 0;

            // 승인 대기(Risk) 개수 계산
            const pendingCount = fraudDetails ? fraudDetails.filter(item => {
                const isFraud = Number(item.isFraud) === 1;
                const engineMsg = item.engine || "";
                const isProcessed = engineMsg.includes("관리자") || engineMsg.includes("거절");
                return isFraud && !isProcessed;
            }).length : 0;

            const blacklistCount = blacklist ? blacklist.length : 0;

            // C. 상단 숫자판 업데이트
            const totalEl = document.getElementById('total-count');
            const fraudEl = document.getElementById('fraud-count');
            const blockedEl = document.getElementById('blocked-count');

            if (totalEl) totalEl.textContent = totalCount;
            if (fraudEl) {
                fraudEl.textContent = pendingCount;
                fraudEl.className = pendingCount > 0 ? "text-warning mb-0 fw-bold" : "text-muted mb-0 fw-bold";
            }
            if (blockedEl) blockedEl.textContent = blacklistCount;

            // D. 각 섹션 렌더링
            UiRenderer.renderHistory(allHistory);
            UiRenderer.renderPending(fraudDetails);
            UiRenderer.renderBlacklist(blacklist);

            // ▼▼▼ [추가] 신고 랭킹도 같이 갱신해야 합니다! ▼▼▼
            this.loadReportRanking();

        } catch (e) {
            console.error("데이터 새로고침 실패:", e);
        }
    },

    // [3] 섹션 전환
    showSection(type, element) {
        const sections = document.querySelectorAll('.content-section');
        sections.forEach(s => {
            s.classList.remove('active-section');
            s.style.display = 'none';
        });

        const target = document.getElementById('section-' + type);
        if (target) {
            target.style.display = 'block';
            target.classList.add('active-section');
        }

        document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
        if (element) element.classList.add('active');

        // 섹션별 데이터 로드
        if (type === 'accounts') this.loadAccounts();
        else if (type === 'policy') this.loadConfigs(); // 설정 탭 누르면 설정값 로드
        else this.refreshAll();

        window.scrollTo(0, 0);
    },

    // [4] 계좌 목록 로드
    async loadAccounts() {
        try {
            const accounts = await FdsApi.fetchAccounts();
            UiRenderer.renderAccounts(accounts);
        } catch (e) { console.error(e); }
    },

    // [5] 설정값 로드 및 화면 표시
    async loadConfigs() {
        try {
            console.log("⚙️ 설정값 불러오는 중...");
            const response = await fetch('/api/v1/admin/config/all');
            const configs = await response.json();

            // HTML 요소 가져오기
            const slider = document.getElementById('threshold-range');
            const display = document.getElementById('threshold-value-display');
            const amountInput = document.getElementById('auto-amount-input');

            // [추가] 텍스트로 보여줄 뱃지 요소
            const thresholdBadge = document.getElementById('current-threshold-badge');
            const amountBadge = document.getElementById('current-amount-badge');

            // DB에서 가져온 리스트를 순회하며 매핑
            configs.forEach(cfg => {
                // 1. 임계치 (THRESHOLD) 처리
                if (cfg.configKey === 'THRESHOLD') {
                    const val = parseFloat(cfg.configValue); // 예: 0.85

                    // 슬라이더와 숫자판 업데이트
                    if (slider) slider.value = Math.round(val * 100); // 85
                    if (display) display.innerText = val.toFixed(2);  // "0.85"

                    // [추가] "현재: 0.85" 텍스트 업데이트
                    if (thresholdBadge) thresholdBadge.innerText = `현재: ${val.toFixed(2)}`;
                }

                // 2. 금액 한도 (AUTO_LIMIT) 처리
                if (cfg.configKey === 'AUTO_LIMIT') {
                    const val = parseInt(cfg.configValue).toLocaleString(); // "1,000,000"

                    // 입력창 업데이트
                    if (amountInput) amountInput.value = cfg.configValue; // 숫자만

                    // [추가] "현재: 1,000,000원" 텍스트 업데이트
                    if (amountBadge) amountBadge.innerText = `현재: ${val}원`;
                }
            });

        } catch (e) {
            console.error("설정 데이터 로드 실패", e);
            alert("설정값을 불러오지 못했습니다.");
        }
    },

    // [6] 신고 랭킹 로드 (HTML 분리 버전)
    async loadReportRanking() {
        try {
            // 백엔드 API 호출
            const response = await fetch('/api/reports/ranking');
            const reports = await response.json();

            // UiRenderer에게 데이터만 던져줌 (HTML 생성 위임)
            UiRenderer.renderReportRanking(reports);

        } catch (e) {
            console.error("랭킹 로드 실패:", e);
        }
    }
};