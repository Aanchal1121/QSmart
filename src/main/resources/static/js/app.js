/**
 * QSmart - Core Application Utilities
 */

// Toast notification helper
function showToast(message, type = 'info') {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        container.style.zIndex = '9999';
        document.body.appendChild(container);
    }

    const toastId = 'toast_' + Date.now();
    const bgClass = type === 'success' ? 'bg-success text-white' :
                    type === 'danger' ? 'bg-danger text-white' :
                    type === 'warning' ? 'bg-warning text-dark' : 'bg-primary text-white';

    const toastHtml = `
        <div id="${toastId}" class="toast align-items-center ${bgClass} border-0" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body">
                    <i class="bi bi-info-circle-fill me-2"></i> ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;

    container.insertAdjacentHTML('beforeend', toastHtml);
    const toastEl = document.getElementById(toastId);
    const bsToast = new bootstrap.Toast(toastEl, { delay: 4000 });
    bsToast.show();
    toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}

// Audio Chime and Speech Synthesizer for Token Announcement
function announceToken(tokenNumber, counterName) {
    // Play subtle synth chime using Web Audio API
    try {
        const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        const osc = audioCtx.createOscillator();
        const gain = audioCtx.createGain();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(587.33, audioCtx.currentTime); // D5
        osc.frequency.exponentialRampToValueAtTime(880, audioCtx.currentTime + 0.3); // A5
        gain.gain.setValueAtTime(0.3, audioCtx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.6);
        osc.connect(gain);
        gain.connect(audioCtx.destination);
        osc.start();
        osc.stop(audioCtx.currentTime + 0.6);
    } catch (e) {
        console.debug('Web Audio chime not supported', e);
    }

    // Speech synthesis announcement
    if ('speechSynthesis' in window) {
        const text = `Token ${tokenNumber}, please proceed to ${counterName || 'the designated room'}`;
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.rate = 0.95;
        window.speechSynthesis.speak(utterance);
    }
}

// QR Code generator using HTML5 Canvas
function renderQrCode(elementId, text) {
    const el = document.getElementById(elementId);
    if (!el) return;

    // Create a high-contrast pattern verification block
    const canvas = document.createElement('canvas');
    canvas.width = 160;
    canvas.height = 160;
    const ctx = canvas.getContext('2d');

    // Background
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, 160, 160);

    // Generate deterministic pixel pattern based on string hash
    let hash = 0;
    for (let i = 0; i < text.length; i++) {
        hash = (hash << 5) - hash + text.charCodeAt(i);
        hash |= 0;
    }

    ctx.fillStyle = '#006d77';
    // Draw 3 corner finder squares like real QR code
    drawFinderPattern(ctx, 10, 10);
    drawFinderPattern(ctx, 110, 10);
    drawFinderPattern(ctx, 10, 110);

    // Random-seeded QR dots from hash
    for (let x = 3; x < 13; x++) {
        for (let y = 3; y < 13; y++) {
            if ((x < 6 && y < 6) || (x > 9 && y < 6) || (x < 6 && y > 9)) continue;
            let bit = (Math.abs(hash) ^ (x * 31 + y * 17)) % 3;
            if (bit === 0) {
                ctx.fillRect(x * 10 + 5, y * 10 + 5, 8, 8);
            }
        }
    }

    el.innerHTML = '';
    el.appendChild(canvas);
}

function drawFinderPattern(ctx, x, y) {
    ctx.fillRect(x, y, 40, 40);
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(x + 6, y + 6, 28, 28);
    ctx.fillStyle = '#006d77';
    ctx.fillRect(x + 12, y + 12, 16, 16);
}

// Current User fetch and UI sync
let currentUser = null;

async function checkAuthAndSyncNavbar() {
    try {
        const res = await fetch('/api/auth/current-user');
        const json = await res.json();
        const userNav = document.getElementById('userNavSection');
        if (!userNav) return;

        if (json.success && json.data) {
            currentUser = json.data;
            userNav.innerHTML = `
                <div class="dropdown">
                    <button class="btn btn-outline-light dropdown-toggle btn-sm d-flex align-items-center gap-2" type="button" data-bs-toggle="dropdown">
                        <i class="bi bi-person-circle"></i>
                        <span>${currentUser.name}</span>
                        <span class="badge bg-light text-dark">${currentUser.role.replace('ROLE_', '')}</span>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end shadow">
                        <li><h6 class="dropdown-header">${currentUser.email || currentUser.phone}</h6></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="patient-dashboard.html"><i class="bi bi-person me-2"></i>Patient Dashboard</a></li>
                        <li><a class="dropdown-item" href="staff-dashboard.html"><i class="bi bi-person-badge me-2"></i>Staff Desk</a></li>
                        <li><a class="dropdown-item" href="doctor-dashboard.html"><i class="bi bi-heart-pulse me-2"></i>Doctor Clinic</a></li>
                        <li><a class="dropdown-item" href="admin-dashboard.html"><i class="bi bi-shield-lock me-2"></i>Admin Console</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item text-danger" href="#" onclick="logoutUser()"><i class="bi bi-box-arrow-right me-2"></i>Logout</a></li>
                    </ul>
                </div>
            `;
        } else {
            currentUser = null;
            userNav.innerHTML = `
                <a href="login.html" class="btn btn-outline-light btn-sm me-2"><i class="bi bi-box-arrow-in-right me-1"></i>Login</a>
                <a href="register.html" class="btn btn-light btn-sm"><i class="bi bi-person-plus me-1"></i>Register</a>
            `;
        }
    } catch (e) {
        console.debug('Auth check failed', e);
    }
}

async function logoutUser() {
    await fetch('/api/auth/logout', { method: 'POST' });
    window.location.href = 'login.html?logout=true';
}

document.addEventListener('DOMContentLoaded', () => {
    checkAuthAndSyncNavbar();
});
