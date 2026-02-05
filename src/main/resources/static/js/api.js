import { API_URLS } from './config.js'; // [핵심] 설정 파일 가져오기

export const FdsApi = {

    // 1. 전체 거래 이력 조회
    async fetchHistory() {
        try {
            // 기존: fetch("/api/v1/transactions/history")
            // 변경: API_URLS.TRANSACTION 사용
            const response = await fetch(`${API_URLS.FRAUD}/all`);
            if (!response.ok) throw new Error('이력 로드 실패');
            return await response.json();
        } catch (error) {
            console.error('API Error (fetchHistory):', error);
            return [];
        }
    },

    // 2. 승인 대기 목록 조회
    async fetchFraudOnly() {
        try {
            const response = await fetch(`${API_URLS.FRAUD}/all`);
            if (!response.ok) return [];
            return await response.json();
        } catch (error) {
            console.error('API Error (fetchFraudOnly):', error);
            return [];
        }
    },

    // 3. 특정 거래 삭제
    async deleteHistory(id) {
        try {
            const res = await fetch(`${API_URLS.TRANSACTION}/${id}`, {
                method: 'DELETE'
            });
            if (!res.ok) throw new Error("삭제 실패");
            return await res.text();
        } catch (error) {
            console.error('API Error (deleteHistory):', error);
            throw error;
        }
    },

    // 4. 계좌 목록 조회
    async fetchAccounts() {
        try {
            const response = await fetch(API_URLS.ACCOUNT);
            if (!response.ok) throw new Error('계좌 로드 실패');
            return await response.json();
        } catch (error) {
            console.error('API Error (fetchAccounts):', error);
            return [];
        }
    },

    // 5. [관리자] 승인
    async approveTransaction(id) {
        try {
            const response = await fetch(`${API_URLS.ADMIN}/approve/${id}`, {
                method: 'POST'
            });
            if (!response.ok) throw new Error(await response.text());
            return await response.text();
        } catch (error) {
            console.error('API Error (approveTransaction):', error);
            throw error;
        }
    },

    // 6. [관리자] 거절
    async rejectTransaction(id) {
        try {
            const response = await fetch(`${API_URLS.ADMIN}/reject/${id}`, {
                method: 'POST'
            });
            if (!response.ok) throw new Error(await response.text());
            return await response.text();
        } catch (error) {
            console.error('API Error (rejectTransaction):', error);
            throw error;
        }
    },

    // 7. 블랙리스트 조회
    async fetchBlacklist() {
        try {
            const response = await fetch(`${API_URLS.ADMIN}/blacklist`);
            if (!response.ok) throw new Error('블랙리스트 로드 실패');
            return await response.json();
        } catch (error) {
            console.error('API Error (fetchBlacklist):', error);
            return [];
        }
    },

    // 8. 블랙리스트 해제
    async removeBlacklist(accountNum) {
        try {
            const response = await fetch(`${API_URLS.ADMIN}/blacklist/${accountNum}`, {
                method: 'DELETE'
            });
            if (!response.ok) throw new Error('차단 해제 실패');
            return await response.text();
        } catch (error) {
            console.error('API Error (removeBlacklist):', error);
            throw error;
        }
    },

    // 9. 규약 설정 가져오기
    async getAllConfigs() {
        try {
            const response = await fetch(`${API_URLS.ADMIN}/config/all`);
            if (!response.ok) throw new Error('설정 로드 실패');
            return await response.json();
        } catch (error) {
            console.error('API Error (getAllConfigs):', error);
            return [];
        }
    },

    // 10. 규약 설정 업데이트
    async updateConfig(key, value) {
        try {
            const response = await fetch(`${API_URLS.ADMIN}/config/update`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    configKey: key,
                    configValue: value.toString()
                })
            });

            if (!response.ok) throw new Error(await response.text());
            return await response.text();
        } catch (error) {
            console.error('API Error (updateConfig):', error);
            throw error;
        }
    }
};

// [신고하기 함수]
export async function createReport(reportData) {
    try {
        const response = await fetch(API_URLS.REPORT, { // 여기도 상수 사용
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(reportData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText);
        }
        return await response.text();
    } catch (error) {
        console.error('신고 API 에러:', error);
        throw error;
    }
}