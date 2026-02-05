// js/app.js 파일 내용
import { UiMain } from './ui-main.js';
import { UiHandler } from './ui-handler.js';

// ▼▼▼ [추가] 보안 체크: 로그인 안 했으면 쫓아내기 ▼▼▼
if (!sessionStorage.getItem('isLoggedIn')) {
    alert("관리자 로그인이 필요합니다.");
    window.location.href = '/login.html'; // 로그인 창으로 강제 이동
}
window.UiMain = UiMain;
window.UiHandler = UiHandler;

document.addEventListener('DOMContentLoaded', () => {
    console.log("🚀 FDS Admin App Initialized");

    UiHandler.initEventHandlers();
    UiMain.init();
});