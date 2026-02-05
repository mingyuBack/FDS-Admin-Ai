// [1] api.js에서 필요한 함수들을 가져옵니다.
import { FdsApi, createReport } from './api.js';

// [2] export를 붙여서 다른 파일(main.js 등)에서 사용할 수 있게 합니다.
export const UiHandler = {
    // 상태 플래그
    isInitialized: false,

    // [헬퍼 함수] UI 새로고침
    refreshUi() {
        if (typeof UiMain !== 'undefined' && UiMain.refreshAll) {
            UiMain.refreshAll();
        } else if (window.UiMain && window.UiMain.refreshAll) {
            window.UiMain.refreshAll();
        } else {
            console.warn("UiMain을 찾을 수 없어 페이지를 새로고침합니다.");
            location.reload();
        }
    },

    // 1. 거래 승인 처리
    async approveTx(id) {
        if (!confirm("이 거래를 승인하시겠습니까?")) return;
        try {
            const msg = await FdsApi.approveTransaction(id);
            alert(msg);
            this.refreshUi();
        } catch (e) { alert("승인 오류: " + e.message); }
    },

    // 2. 거래 거절 및 블랙리스트 등록
    async rejectTx(id) {
        if (!confirm("거래를 거절하고 수취인 계좌를 블랙리스트에 등록하시겠습니까?")) return;
        try {
            const msg = await FdsApi.rejectTransaction(id);
            alert(msg);
            this.refreshUi();
        } catch (e) { alert("거절 오류: " + e.message); }
    },

    // 3. 블랙리스트 해제
    async removeFromBlacklist(accountNum) {
        if (!confirm(`계좌 [${accountNum}]의 차단을 해제하시겠습니까?`)) return;
        try {
            const msg = await FdsApi.removeBlacklist(accountNum);
            alert(msg);
            this.refreshUi();
        } catch (e) { alert("해제 오류: " + e.message); }
    },

    // 4. 정책 설정 통합 저장
    async saveAllConfigs() {
        const thresholdEl = document.getElementById('threshold-range');
        const amountEl = document.getElementById('auto-amount-input');

        if (!thresholdEl || !amountEl) {
            alert("설정 항목을 찾을 수 없습니다.");
            return;
        }

        const rawThreshold = thresholdEl.value;
        const thresholdVal = (rawThreshold / 100).toFixed(2);
        const amountVal = amountEl.value;

        if (!amountVal) {
            alert("자동 승인 기준 금액을 입력해주세요.");
            return;
        }

        const btn = document.getElementById('btn-save-policy');
        if(btn) {
            btn.disabled = true;
            btn.innerText = "저장 중...";
        }

        try {
            await Promise.all([
                FdsApi.updateConfig('THRESHOLD', thresholdVal),
                FdsApi.updateConfig('AUTO_LIMIT', amountVal)
            ]);

            alert(`✅ 모든 정책 설정이 성공적으로 저장되었습니다.\n(임계치: ${thresholdVal}, 금액: ${amountVal})`);
            this.refreshUi();

        } catch (e) {
            alert("❌ 저장 실패: " + e.message);
        } finally {
            if(btn) {
                btn.disabled = false;
                btn.innerText = "정책 설정 반영하기";
            }
        }
    },

    // 5. 기록 삭제 처리
    async handleDeleteItem(id) {
        if (!id) { alert("ID가 유효하지 않습니다."); return; }
        if (!confirm("정말 삭제하시겠습니까?")) return;

        try {
            const msg = await FdsApi.deleteHistory(id);
            alert(msg);
            this.refreshUi();
        } catch (e) {
            console.error(e);
            alert("삭제 실패: " + e.message);
        }
    },

    // 6. 송금 테스트
    async handleTransfer(txData) {
        try {
            const response = await fetch('/api/v1/transactions', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(txData)
            });

            if (!response.ok) throw new Error("거래 요청 실패");
            const result = await response.json();

            if (result.isFraud === 1) {
                alert("🚨 이상 거래(한도 초과/AI 의심)로 감지되어 승인 대기 상태로 전환되었습니다.");
            } else {
                alert("✅ 송금이 즉시 완료되었습니다.");
            }
            this.refreshUi();
        } catch (e) {
            alert("송금 오류: " + e.message);
        }
    },

    // 7. 이벤트 리스너 초기화
    initEventHandlers() {
        if (this.isInitialized) return;
        this.isInitialized = true;

        console.log("✅ UiHandler 이벤트 리스너 초기화됨");

        document.body.addEventListener('click', (e) => {
            // 1. [차단 버튼]
            const blockBtn = e.target.closest('.btn-blacklist-add');
            if (blockBtn) {
                const account = blockBtn.dataset.account;
                if (account) this.handleAddToBlacklist(account);
                return;
            }

            // 2. [신고 제출 버튼] (모달 내부)
            if (e.target.id === 'btn-submit-report') {
                this.handleSubmitReport();
                return;
            }

            // 3. [정책 저장 버튼]
            if (e.target.id === 'btn-save-policy') {
                this.saveAllConfigs();
                return;
            }

            // 4. [재학습 버튼]
            if (e.target.id === 'btn-retrain') {
                if (!confirm("⚠️ 현재까지 쌓인 데이터로 AI를 재학습 시키시겠습니까?\n(데이터가 적으면 실패할 수 있습니다)")) {
                    return;
                }
                this.handleRetrain();
                return;
            }

            // 5. [로그아웃 버튼]
            const logoutBtn = e.target.closest('#btn-logout');
            if (logoutBtn) {
                if(confirm("로그아웃 하시겠습니까?")) {
                    sessionStorage.removeItem('isLoggedIn');
                    window.location.href = '/login.html';
                }
                return;
            }

            // ▼▼▼ [핵심] 신고 모달 열기 버튼 감지 ▼▼▼
            // class="btn-open-report-modal"을 가진 버튼을 찾습니다.
            const openReportBtn = e.target.closest('.btn-open-report-modal');
            if (openReportBtn) {
                const txId = openReportBtn.dataset.id;
                const accountNum = openReportBtn.dataset.account; // 화면 표시용 (전송은 안함)

                // 1. 모달 텍스트 업데이트 (사용자 확인용)
                const displayEl = document.getElementById('modal-tx-display');
                if (displayEl) displayEl.innerText = txId; // 혹은 accountNum을 보여줘도 됨

                // 2. 히든 인풋에 ID 주입
                const txIdInput = document.getElementById('report-tx-id');
                if (txIdInput) txIdInput.value = txId;

                // 3. 모달 띄우기 (Bootstrap)
                const modalEl = document.getElementById('reportModal');
                if (modalEl && window.bootstrap) {
                    const modal = new bootstrap.Modal(modalEl);
                    modal.show();
                }
            }
        });

        // 슬라이더 감지
        document.body.addEventListener('input', (e) => {
            if (e.target.id === 'threshold-range') {
                const val = (e.target.value / 100).toFixed(2);
                const display = document.getElementById('threshold-value-display');
                if (display) display.innerText = val;
            }
        });
    },

    // 8. 블랙리스트 추가 함수
    async handleAddToBlacklist(account) {
        if (!confirm(`[${account}] 계좌를 정말로 차단하시겠습니까?`)) return;

        try {
            const response = await fetch('/api/v1/admin/blacklist', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    accountNum: account,
                    reason: "신고 누적으로 인한 관리자 차단"
                })
            });

            if (response.ok) {
                alert(`✅ [${account}] 차단 완료!`);
                this.refreshUi();
            } else {
                const msg = await response.text();
                alert("⚠️ 실패: " + msg);
            }
        } catch (e) {
            console.error(e);
            alert("❌ 서버 통신 오류");
        }
    },

    // ▼▼▼ [수정됨] 신고 제출 함수 (ID만 전송) ▼▼▼
    async handleSubmitReport() {
        const txIdEl = document.getElementById('report-tx-id');
        const reasonEl = document.getElementById('report-reason');

        if (!txIdEl || !txIdEl.value) {
            alert("신고할 거래 정보(ID)가 없습니다.");
            return;
        }

        try {
            // 백엔드가 ID로 계좌를 찾으므로 transactionId만 보냅니다.
            await createReport({
                transactionId: txIdEl.value,
                reason: reasonEl ? reasonEl.value : "기타"
            });

            alert("✅ 신고가 접수되었습니다.");

            // 모달 닫기
            const closeBtn = document.querySelector('#reportModal .btn-close');
            if(closeBtn) closeBtn.click();

            this.refreshUi();

        } catch (e) {
            alert("신고 처리 중 오류: " + e.message);
        }
    },

    // 9. 재학습 요청 함수
    async handleRetrain() {
        try {
            const response = await fetch('/api/v1/admin/retrain', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });

            const msg = await response.text();
            alert(msg);

        } catch (error) {
            console.error(error);
            alert("재학습 요청 중 오류가 발생했습니다.");
        }
    }
};