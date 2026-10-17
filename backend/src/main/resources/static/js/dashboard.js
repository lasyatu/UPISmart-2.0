// =========================================================================
// UPISmart 2.0 — Cross-Platform Web Client & PWA Engine
// Synchronizes with Java Spring Boot REST API & Amazon RDS MySQL
// =========================================================================

const API_BASE = '/api';
const API_KEY = 'upismart-demo-secure-key-2026';

let appState = {
    user: null,
    budget: null,
    transactions: [],
    bills: [],
    vault: null,
    insights: null
};

let deferredPrompt = null;

// Initialization
document.addEventListener('DOMContentLoaded', () => {
    initApp();
    setupPwa();
    setupNavigation();
});

// PWA Service Worker & Install Prompt Setup
function setupPwa() {
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('/sw.js')
            .then(reg => console.log('UPISmart PWA Service Worker registered:', reg.scope))
            .catch(err => console.log('Service Worker registration skipped:', err));
    }

    window.addEventListener('beforeinstallprompt', (e) => {
        e.preventDefault();
        deferredPrompt = e;
        const installBtn = document.getElementById('pwaInstallBtn');
        if (installBtn) installBtn.style.display = 'inline-flex';
    });
}

function triggerPwaInstall() {
    if (deferredPrompt) {
        deferredPrompt.prompt();
        deferredPrompt.userChoice.then((choiceResult) => {
            if (choiceResult.outcome === 'accepted') {
                console.log('User installed UPISmart PWA');
            }
            deferredPrompt = null;
            document.getElementById('pwaInstallBtn').style.display = 'none';
        });
    }
}

// Navigation Tab Management
function setupNavigation() {
    const navItems = document.querySelectorAll('#mainNav .nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const view = item.getAttribute('data-view');
            switchView(view);
            window.location.hash = view;
        });
    });

    // Check initial hash
    const initialView = window.location.hash.replace('#', '') || 'dashboard';
    switchView(initialView);
}

function switchView(viewName) {
    const views = ['dashboard', 'transactions', 'vault', 'bills', 'detective', 'bucks', 'scanpay', 'settings'];
    if (!views.includes(viewName)) viewName = 'dashboard';

    // Update active nav item
    document.querySelectorAll('#mainNav .nav-item').forEach(item => {
        if (item.getAttribute('data-view') === viewName) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });

    // Toggle view sections
    views.forEach(v => {
        const sec = document.getElementById(`view-${v}`);
        if (sec) sec.classList.toggle('active', v === viewName);
    });

    // Update title
    const titles = {
        dashboard: { title: 'Financial Dashboard', sub: 'Spring Boot 3 + Amazon RDS MySQL + Cross-Platform PWA' },
        transactions: { title: 'Transaction Ledger', sub: 'Auditable simulated UPI activity log' },
        vault: { title: 'Emergency Vault', sub: 'PIN-secured emergency reserve management' },
        bills: { title: 'Bills Radar', sub: 'Scheduled monthly obligations & reservation tracker' },
        detective: { title: 'Spending Detective', sub: 'Transparent rule-based financial health heuristics' },
        bucks: { title: 'Bucks AI Companion', sub: 'Witty Gen-Z personal financial assistant' },
        scanpay: { title: 'Simulated QR Pay', sub: 'Camera viewfinder simulator for demo UPI transfers' },
        settings: { title: 'Settings & Cloud Spec', sub: 'AWS Cloud deployment and financial data policies' }
    };

    if (titles[viewName]) {
        document.getElementById('viewTitle').textContent = titles[viewName].title;
        document.getElementById('viewSubtitle').textContent = titles[viewName].sub;
    }
}

// App Initialization & Data Fetching
async function initApp() {
    await checkHealth();
    await loadDashboardData();
}

async function checkHealth() {
    const pill = document.getElementById('connectionStatusPill');
    const text = document.getElementById('connectionStatusText');

    try {
        const res = await fetch(`${API_BASE}/health`);
        if (!res.ok) throw new Error('Health check failed');
        const data = await res.json();

        if (data.database && data.database.connected) {
            pill.className = 'status-pill status-online';
            text.textContent = `Connected: ${data.database.databaseProductName || 'MySQL'}`;
        } else {
            pill.className = 'status-pill status-local';
            text.textContent = 'Local Engine (Ready for AWS RDS)';
        }
    } catch (err) {
        pill.className = 'status-pill status-local';
        text.textContent = 'Backend Offline';
    }
}

async function loadDashboardData() {
    await Promise.all([
        fetchUserProfile(),
        fetchBudgetUtilization(),
        fetchTransactions(),
        fetchVaultStatus(),
        fetchBills(),
        fetchInsights()
    ]);
}

async function fetchUserProfile() {
    try {
        const res = await fetch(`${API_BASE}/user/profile`);
        if (res.ok) {
            appState.user = await res.json();
            document.getElementById('sidebarUsername').textContent = appState.user.username;
            document.getElementById('sidebarUpi').textContent = appState.user.upiHandle;
        }
    } catch (e) {
        console.error('Error fetching user profile:', e);
    }
}

async function fetchBudgetUtilization() {
    try {
        const res = await fetch(`${API_BASE}/budget/utilization`);
        if (!res.ok) return;
        appState.budget = await res.json();

        document.getElementById('monthlySalaryDisplay').textContent = formatCurrency(appState.budget.monthlyIncome);
        document.getElementById('totalExpenditureDisplay').textContent = formatCurrency(appState.budget.totalExpenditure);
        document.getElementById('remainingBalanceDisplay').textContent = formatCurrency(appState.budget.remainingBalance);
        document.getElementById('dailySafeToSpendDisplay').textContent = formatCurrency(appState.budget.dailySafeToSpend);

        document.getElementById('spentPercentageDisplay').textContent = `${appState.budget.percentageSpent.toFixed(1)}% spent`;
        document.getElementById('remainingPercentageDisplay').textContent = `${appState.budget.percentageRemaining.toFixed(1)}% remaining`;

        // Update Meter Bar
        const fill = document.getElementById('meterProgressFill');
        const fillWidth = Math.min(100, Math.max(0, appState.budget.percentageSpent));
        fill.style.width = `${fillWidth}%`;

        // Update Status Badge
        const badge = document.getElementById('utilizationStatusBadge');
        badge.textContent = appState.budget.utilizationStatus;
        badge.className = 'badge';
        if (appState.budget.utilizationStatus === 'OPTIMAL') badge.classList.add('badge-success');
        else if (appState.budget.utilizationStatus === 'WARNING') badge.classList.add('badge-warning');
        else badge.classList.add('badge-danger');

        // Update Category Tags
        const tagsContainer = document.getElementById('categoryTagsContainer');
        tagsContainer.innerHTML = '';
        if (appState.budget.categoryBreakdown && Object.keys(appState.budget.categoryBreakdown).length > 0) {
            for (const [cat, amt] of Object.entries(appState.budget.categoryBreakdown)) {
                const tag = document.createElement('span');
                tag.className = 'category-tag';
                tag.textContent = `${cat}: ${formatCurrency(amt)}`;
                tagsContainer.appendChild(tag);
            }
        } else {
            tagsContainer.innerHTML = '<span class="category-tag">No outflows recorded yet</span>';
        }
    } catch (e) {
        console.error('Error fetching budget:', e);
    }
}

async function fetchTransactions() {
    try {
        const res = await fetch(`${API_BASE}/transactions`);
        if (!res.ok) return;
        appState.transactions = await res.json();
        renderTransactions();
    } catch (e) {
        console.error('Error fetching transactions:', e);
    }
}

function renderTransactions() {
    const recentBody = document.getElementById('recentTransactionsTableBody');
    const fullBody = document.getElementById('fullTransactionTableBody');

    // Recent activity (dashboard)
    recentBody.innerHTML = '';
    const recent = appState.transactions.slice(0, 5);
    if (recent.length === 0) {
        recentBody.innerHTML = '<tr><td colspan="3" class="empty-cell">No transactions yet. Click "+ New UPI Payment" to test!</td></tr>';
    } else {
        recent.forEach(tx => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>
                    <strong>${escapeHtml(tx.recipientName || 'Recipient')}</strong><br>
                    <small style="color: var(--text-muted);">${escapeHtml(tx.recipientUpi)}</small>
                </td>
                <td><span class="category-tag">${escapeHtml(tx.category || 'Other')}</span></td>
                <td><strong style="color: var(--danger);">- ${formatCurrency(tx.amount)}</strong></td>
            `;
            recentBody.appendChild(row);
        });
    }

    // Full transactions table
    filterTransactions();
}

function filterTransactions() {
    const fullBody = document.getElementById('fullTransactionTableBody');
    const search = (document.getElementById('txSearchInput')?.value || '').toLowerCase();
    const category = document.getElementById('txCategoryFilter')?.value || 'ALL';

    const filtered = appState.transactions.filter(tx => {
        const matchSearch = (tx.recipientName && tx.recipientName.toLowerCase().includes(search)) ||
                            (tx.recipientUpi && tx.recipientUpi.toLowerCase().includes(search)) ||
                            (tx.referenceId && tx.referenceId.toLowerCase().includes(search));
        const matchCat = category === 'ALL' || tx.category === category;
        return matchSearch && matchCat;
    });

    fullBody.innerHTML = '';
    if (filtered.length === 0) {
        fullBody.innerHTML = '<tr><td colspan="5" class="empty-cell">No matching transactions found</td></tr>';
        return;
    }

    filtered.forEach(tx => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>
                <strong>${escapeHtml(tx.recipientName || 'Recipient')}</strong><br>
                <small style="color: var(--text-muted);">${escapeHtml(tx.recipientUpi)}</small>
            </td>
            <td><span class="category-tag">${escapeHtml(tx.category || 'Other')}</span></td>
            <td><code style="font-family: 'JetBrains Mono', monospace; font-size: 11px; color: var(--lime);">${escapeHtml(tx.referenceId)}</code></td>
            <td><strong style="color: var(--danger);">- ${formatCurrency(tx.amount)}</strong></td>
            <td><span class="badge badge-success">${escapeHtml(tx.status)}</span></td>
        `;
        fullBody.appendChild(row);
    });
}

async function fetchVaultStatus() {
    try {
        const res = await fetch(`${API_BASE}/vault`);
        if (!res.ok) return;
        appState.vault = await res.json();

        document.getElementById('vaultReserveDisplay').textContent = formatCurrency(appState.vault.reserveBalance);
        document.getElementById('vaultReserveDisplayDetail').textContent = formatCurrency(appState.vault.reserveBalance);

        const badge1 = document.getElementById('vaultStatusBadge');
        const badge2 = document.getElementById('vaultStatusBadgeDetail');
        const statusText = appState.vault.isLocked ? 'LOCKED' : 'UNLOCKED';
        const statusClass = appState.vault.isLocked ? 'badge badge-secondary' : 'badge badge-success';

        badge1.textContent = statusText;
        badge1.className = statusClass;
        badge2.textContent = statusText;
        badge2.className = statusClass;

        // Render audit logs
        const auditList = document.getElementById('vaultAuditList');
        auditList.innerHTML = '';
        if (!appState.vault.auditLogs || appState.vault.auditLogs.length === 0) {
            auditList.innerHTML = '<div class="audit-item">No audit records yet</div>';
            return;
        }

        appState.vault.auditLogs.forEach(log => {
            const item = document.createElement('div');
            item.className = 'audit-item';
            item.innerHTML = `
                <span><strong>${escapeHtml(log.actionType)}</strong>: ${log.amount > 0 ? formatCurrency(log.amount) : ''} (${escapeHtml(log.failureReason || 'Success')})</span>
                <span style="color: var(--text-muted);">${escapeHtml(log.timestamp)}</span>
            `;
            auditList.appendChild(item);
        });
    } catch (e) {
        console.error('Error fetching vault:', e);
    }
}

async function fetchBills() {
    const tbody = document.getElementById('billsTableBody');
    try {
        const res = await fetch(`${API_BASE}/bills`);
        if (!res.ok) return;
        appState.bills = await res.json();

        tbody.innerHTML = '';
        if (appState.bills.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="empty-cell">No scheduled bills found. Click "+ Add New Bill" to create one.</td></tr>';
            return;
        }

        appState.bills.forEach(bill => {
            const row = document.createElement('tr');
            const isPaid = bill.isPaid;
            const statusBadge = isPaid
                ? '<span class="badge badge-success">PAID</span>'
                : (bill.isOverdue ? '<span class="badge badge-danger">OVERDUE</span>' : '<span class="badge badge-warning">DUE SOON</span>');

            const actionBtn = isPaid
                ? '<span style="color: var(--text-muted); font-size: 12px;">Settled</span>'
                : `<button class="btn btn-sm btn-outline" onclick="payBill(${bill.billId})">Mark as Paid</button>`;

            row.innerHTML = `
                <td><strong>${escapeHtml(bill.title)}</strong></td>
                <td><span class="category-tag">${escapeHtml(bill.category)}</span></td>
                <td>Day ${bill.dueDay} of month</td>
                <td><strong>${formatCurrency(bill.amount)}</strong></td>
                <td>${statusBadge}</td>
                <td>${actionBtn}</td>
            `;
            tbody.appendChild(row);
        });
    } catch (e) {
        console.error('Error fetching bills:', e);
    }
}

async function payBill(billId) {
    if (!confirm('Execute simulated payment for this bill obligation?')) return;
    try {
        const res = await fetch(`${API_BASE}/bills/${billId}/pay`, { method: 'PUT' });
        if (res.ok) {
            alert('Bill marked as paid and logged into transaction history!');
            await loadDashboardData();
        } else {
            alert('Failed to pay bill');
        }
    } catch (err) {
        alert('Error paying bill: ' + err.message);
    }
}

async function fetchInsights() {
    try {
        const res = await fetch(`${API_BASE}/insights`);
        if (!res.ok) return;
        appState.insights = await res.json();

        document.getElementById('detectiveVelocityDisplay').textContent = `${appState.insights.spendingVelocityPercent.toFixed(1)}%`;
        document.getElementById('detectiveBillsDisplay').textContent = formatCurrency(appState.insights.billsReserved);
        document.getElementById('detectiveTopCatDisplay').textContent = appState.insights.topSpendingCategory || 'None';
        document.getElementById('detectiveTopCatAmt').textContent = formatCurrency(appState.insights.topCategoryAmount);
        document.getElementById('detectivePeakDebitDisplay').textContent = appState.insights.largestDebitTitle || 'None';
        document.getElementById('detectivePeakDebitAmt').textContent = formatCurrency(appState.insights.largestDebitAmount);

        const healthBadge = document.getElementById('detectiveHealthBadge');
        if (appState.insights.isVelocityWarning) {
            healthBadge.textContent = 'HIGH VELOCITY';
            healthBadge.className = 'badge badge-danger';
        } else {
            healthBadge.textContent = 'OPTIMAL VELOCITY';
            healthBadge.className = 'badge badge-success';
        }

        const list = document.getElementById('detectiveRulesList');
        list.innerHTML = '';
        appState.insights.ruleDetections.forEach(det => {
            const box = document.createElement('div');
            box.className = 'alert-box alert-warning';
            box.textContent = det;
            list.appendChild(box);
        });
    } catch (e) {
        console.error('Error fetching insights:', e);
    }
}

// Bucks AI Conversation Engine
function sendBucksQuery(text) {
    document.getElementById('bucksUserInput').value = text;
    submitBucksMessage();
}

function submitBucksMessage() {
    const input = document.getElementById('bucksUserInput');
    const text = input.value.trim();
    if (!text) return;

    appendBucksMessage(text, 'user');
    input.value = '';

    // Generate intelligent response grounded in real data
    setTimeout(() => {
        const response = generateBucksResponse(text);
        appendBucksMessage(response, 'bucks');
    }, 450);
}

function appendBucksMessage(content, sender) {
    const window = document.getElementById('bucksChatWindow');
    const bubble = document.createElement('div');
    bubble.className = `chat-bubble chat-${sender}`;
    bubble.innerHTML = escapeHtml(content);
    window.appendChild(bubble);
    window.scrollTop = window.scrollHeight;
}

function generateBucksResponse(query) {
    const q = query.toLowerCase();
    const safeDaily = appState.budget ? appState.budget.dailySafeToSpend : 1166;
    const remaining = appState.budget ? appState.budget.remainingBalance : 50000;
    const topCat = appState.insights ? appState.insights.topSpendingCategory : 'None';
    const reservedBills = appState.insights ? appState.insights.billsReserved : 0;

    if (q.includes('safe') || q.includes('spend') || q.includes('daily') || q.includes('today')) {
        return `Yo! Your current deterministic Safe-to-Spend limit is ${formatCurrency(safeDaily)} per day. Stick to that pace and you'll crush your monthly targets, no cap.`;
    }
    if (q.includes('bill') || q.includes('due') || q.includes('radar')) {
        return `You've got ${formatCurrency(reservedBills)} reserved for upcoming bills this cycle. Don't touch that cash — it's locked to keep your lights on!`;
    }
    if (q.includes('where') || q.includes('category') || q.includes('biggest') || q.includes('food')) {
        return `Your biggest outflow category right now is '${topCat}'. Keep an eye on impulse dining and snack runs, fr.`;
    }
    if (q.includes('afford') || q.includes('dinner') || q.includes('3000') || q.includes('3,000')) {
        if (safeDaily >= 3000) {
            return `You're looking good! ${formatCurrency(3000)} is within your available balance. Treat yourself, but balance it out tomorrow!`;
        } else {
            return `Hol' up! ₹3,000 is higher than your daily Safe-to-Spend limit of ${formatCurrency(safeDaily)}. You can technically do it, but you'll have to tighten up for the next few days.`;
        }
    }
    return `Facts: You have ${formatCurrency(remaining)} remaining this month. Safe-to-Spend pace is ${formatCurrency(safeDaily)}/day. Ask me anything about bills, vaults, or spending pace!`;
}

// Payment Modal Logic
function openPaymentModal() {
    document.getElementById('paymentModal').classList.add('active');
    document.getElementById('unusualSpendingWarning').style.display = 'none';
    document.getElementById('userConfirmCheckbox').checked = false;
}

function closePaymentModal() {
    document.getElementById('paymentModal').classList.remove('active');
}

async function handlePaymentSubmit(event) {
    event.preventDefault();
    const btn = document.getElementById('submitPaymentBtn');
    const warningBox = document.getElementById('unusualSpendingWarning');
    const confirmCheckbox = document.getElementById('userConfirmCheckbox');

    const payload = {
        recipientUpi: document.getElementById('recipientUpi').value,
        recipientName: document.getElementById('recipientName').value,
        amount: parseFloat(document.getElementById('paymentAmount').value),
        category: document.getElementById('paymentCategory').value,
        enteredPin: document.getElementById('paymentPin').value,
        note: document.getElementById('paymentNote').value,
        userConfirmed: confirmCheckbox.checked
    };

    btn.disabled = true;
    btn.textContent = 'Processing Transfer...';

    try {
        const res = await fetch(`${API_BASE}/transactions`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-API-KEY': API_KEY
            },
            body: JSON.stringify(payload)
        });

        const data = await res.json();

        if (res.status === 409 && data.details && data.details.requiresConfirmation) {
            warningBox.style.display = 'block';
            document.getElementById('unusualSpendingMessage').textContent = data.message;
            btn.disabled = false;
            btn.textContent = 'Confirm & Pay (Override Rule)';
            return;
        }

        if (!res.ok) {
            alert(`Payment Failed: ${data.message || 'Invalid transaction'}`);
            btn.disabled = false;
            btn.textContent = 'Confirm & Pay';
            return;
        }

        closePaymentModal();
        alert(`Payment Success! Reference: ${data.referenceId}\n${data.message}`);
        await loadDashboardData();
    } catch (err) {
        alert(`Error executing payment: ${err.message}`);
    } finally {
        btn.disabled = false;
        btn.textContent = 'Confirm & Pay';
    }
}

// QR Scan & Pay Logic
function loadSampleQr() {
    document.getElementById('qrScannedUri').value = 'upismart://pay?recipient=cafe@demo&name=Artisan%20Coffee%20Roasters';
    document.getElementById('qrPayAmount').value = '280.00';
    alert('Sample Demo QR loaded successfully into scanner!');
}

async function handleQrPaySubmit(event) {
    event.preventDefault();
    const uri = document.getElementById('qrScannedUri').value;
    const amount = parseFloat(document.getElementById('qrPayAmount').value);
    const pin = document.getElementById('qrPayPin').value;

    // Strict validation: must be upismart://pay and recipient must end with @demo
    if (!uri.startsWith('upismart://pay?')) {
        alert('Invalid QR code format! Only fictional demo QR codes (upismart://pay?...) are permitted.');
        return;
    }

    const urlParams = new URLSearchParams(uri.split('?')[1]);
    const recipient = urlParams.get('recipient') || 'merchant@demo';
    const name = urlParams.get('name') || 'Demo Merchant';

    if (!recipient.endsWith('@demo')) {
        alert('Security rule violation: Recipient must end with @demo. External or real UPI banking targets are forbidden.');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/transactions`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-API-KEY': API_KEY
            },
            body: JSON.stringify({
                recipientUpi: recipient,
                recipientName: name,
                amount: amount,
                category: 'Food & Dining',
                enteredPin: pin,
                note: 'Simulated QR Scan Transfer',
                userConfirmed: true
            })
        });

        const data = await res.json();
        if (res.ok) {
            alert(`Simulated Transfer Successful!\nReference: ${data.referenceId}\nRecipient: ${name} (${recipient})\nAmount: ${formatCurrency(amount)}`);
            await loadDashboardData();
            switchView('dashboard');
        } else {
            alert(`Transfer Failed: ${data.message}`);
        }
    } catch (err) {
        alert('Error: ' + err.message);
    }
}

// Vault Modal Logic
function openVaultModal(type) {
    document.getElementById('vaultActionType').value = type;
    document.getElementById('vaultModalTitle').textContent = type === 'deposit' ? 'Deposit Emergency Reserve' : 'Withdraw Emergency Funds';
    document.getElementById('vaultPinGroup').style.display = type === 'withdraw' ? 'block' : 'none';
    document.getElementById('submitVaultBtn').textContent = type === 'deposit' ? 'Deposit' : 'Withdraw';
    document.getElementById('vaultModal').classList.add('active');
}

function closeVaultModal() {
    document.getElementById('vaultModal').classList.remove('active');
}

async function handleVaultSubmit(event) {
    event.preventDefault();
    const actionType = document.getElementById('vaultActionType').value;
    const amount = parseFloat(document.getElementById('vaultAmount').value);
    const pin = document.getElementById('vaultPin').value;

    const endpoint = actionType === 'deposit' ? `${API_BASE}/vault/deposit` : `${API_BASE}/vault/withdraw`;
    const payload = { amount, pin };

    try {
        const res = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-API-KEY': API_KEY
            },
            body: JSON.stringify(payload)
        });
        const data = await res.json();

        if (!res.ok) {
            alert(`Vault Operation Failed: ${data.message || 'Error'}`);
            return;
        }

        closeVaultModal();
        alert(`Vault reserve updated! Current balance: ${formatCurrency(data.reserveBalance)}`);
        await loadDashboardData();
    } catch (err) {
        alert(`Vault error: ${err.message}`);
    }
}

// Add Bill Modal Logic
function openAddBillModal() {
    document.getElementById('billModal').classList.add('active');
}

function closeBillModal() {
    document.getElementById('billModal').classList.remove('active');
}

async function handleAddBillSubmit(event) {
    event.preventDefault();
    const payload = {
        title: document.getElementById('billTitle').value,
        category: document.getElementById('billCategory').value,
        amount: parseFloat(document.getElementById('billAmount').value),
        dueDay: parseInt(document.getElementById('billDueDay').value),
        billerUpi: 'billing@demo.upismart'
    };

    try {
        const res = await fetch(`${API_BASE}/bills`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-API-KEY': API_KEY
            },
            body: JSON.stringify(payload)
        });
        if (res.ok) {
            closeBillModal();
            alert('Bill successfully scheduled on Bills Radar!');
            await loadDashboardData();
        } else {
            const err = await res.json();
            alert(`Failed to add bill: ${err.message}`);
        }
    } catch (err) {
        alert('Error: ' + err.message);
    }
}

// Helpers
function formatCurrency(val) {
    if (val === undefined || val === null || isNaN(val)) return '₹0.00';
    return '₹' + Number(val).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]));
}
