// UPISmart 2.0 Web Financial Dashboard JS
const API_BASE = '/api';

document.addEventListener('DOMContentLoaded', () => {
    initDashboard();
});

function initDashboard() {
    checkHealth();
    loadDashboardData();
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
            text.textContent = 'Local Engine (Ready for Cloud SQL)';
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
        fetchVaultStatus()
    ]);
}

async function fetchUserProfile() {
    try {
        const res = await fetch(`${API_BASE}/user/profile`);
        if (res.ok) {
            const user = await res.json();
            document.getElementById('sidebarUsername').textContent = user.username;
            document.getElementById('sidebarUpi').textContent = user.upiHandle;
        }
    } catch (e) {
        console.error('Error fetching user profile:', e);
    }
}

async function fetchBudgetUtilization() {
    try {
        const res = await fetch(`${API_BASE}/budget/utilization`);
        if (!res.ok) return;
        const data = await res.json();

        document.getElementById('monthlySalaryDisplay').textContent = formatCurrency(data.monthlyIncome);
        document.getElementById('totalExpenditureDisplay').textContent = formatCurrency(data.totalExpenditure);
        document.getElementById('remainingBalanceDisplay').textContent = formatCurrency(data.remainingBalance);
        document.getElementById('dailySafeToSpendDisplay').textContent = formatCurrency(data.dailySafeToSpend);

        document.getElementById('spentPercentageDisplay').textContent = `${data.percentageSpent.toFixed(1)}% spent`;
        document.getElementById('remainingPercentageDisplay').textContent = `${data.percentageRemaining.toFixed(1)}% remaining`;

        // Update Meter Bar
        const fill = document.getElementById('meterProgressFill');
        const fillWidth = Math.min(100, Math.max(0, data.percentageSpent));
        fill.style.width = `${fillWidth}%`;

        // Update Status Badge
        const badge = document.getElementById('utilizationStatusBadge');
        badge.textContent = data.utilizationStatus;
        badge.className = 'badge';
        if (data.utilizationStatus === 'OPTIMAL') badge.classList.add('badge-success');
        else if (data.utilizationStatus === 'WARNING') badge.classList.add('badge-warning');
        else badge.classList.add('badge-danger');

        // Update Category Tags
        const tagsContainer = document.getElementById('categoryTagsContainer');
        tagsContainer.innerHTML = '';
        if (data.categoryBreakdown && Object.keys(data.categoryBreakdown).length > 0) {
            for (const [cat, amt] of Object.entries(data.categoryBreakdown)) {
                const tag = document.createElement('span');
                tag.className = 'category-tag';
                tag.textContent = `${cat}: ${formatCurrency(amt)}`;
                tagsContainer.appendChild(tag);
            }
        } else {
            tagsContainer.innerHTML = '<span class="category-tag">No expenses yet</span>';
        }
    } catch (e) {
        console.error('Error fetching budget:', e);
    }
}

async function fetchTransactions() {
    const tbody = document.getElementById('transactionTableBody');
    try {
        const res = await fetch(`${API_BASE}/transactions`);
        if (!res.ok) return;
        const txs = await res.json();

        tbody.innerHTML = '';
        if (!txs || txs.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="empty-cell">No transactions recorded yet. Tap "+ New UPI Payment" to test!</td></tr>';
            return;
        }

        txs.forEach(tx => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>
                    <strong>${escapeHtml(tx.recipientName || 'Recipient')}</strong><br>
                    <small style="color: #94a3b8;">${escapeHtml(tx.recipientUpi)}</small>
                </td>
                <td><span class="category-tag">${escapeHtml(tx.category || 'Other')}</span></td>
                <td><code>${escapeHtml(tx.referenceId)}</code></td>
                <td><strong style="color: #ef4444;">- ${formatCurrency(tx.amount)}</strong></td>
                <td><span class="badge badge-success">${escapeHtml(tx.status)}</span></td>
            `;
            tbody.appendChild(row);
        });
    } catch (e) {
        console.error('Error fetching transactions:', e);
        tbody.innerHTML = '<tr><td colspan="5" class="empty-cell">Failed to load transactions</td></tr>';
    }
}

async function fetchVaultStatus() {
    try {
        const res = await fetch(`${API_BASE}/vault`);
        if (!res.ok) return;
        const vault = await res.json();

        document.getElementById('vaultReserveDisplay').textContent = formatCurrency(vault.reserveBalance);
        const badge = document.getElementById('vaultStatusBadge');
        badge.textContent = vault.isLocked ? 'LOCKED' : 'UNLOCKED';
        badge.className = vault.isLocked ? 'badge badge-secondary' : 'badge badge-success';

        // Render audit logs
        const auditList = document.getElementById('vaultAuditList');
        auditList.innerHTML = '';
        if (!vault.auditLogs || vault.auditLogs.length === 0) {
            auditList.innerHTML = '<div class="audit-item">No audit logs yet</div>';
            return;
        }

        vault.auditLogs.forEach(log => {
            const item = document.createElement('div');
            item.className = 'audit-item';
            item.innerHTML = `
                <span><strong>${escapeHtml(log.actionType)}</strong>: ${log.amount > 0 ? formatCurrency(log.amount) : ''} (${escapeHtml(log.failureReason || 'Success')})</span>
                <span style="color: #94a3b8;">${escapeHtml(log.timestamp)}</span>
            `;
            auditList.appendChild(item);
        });
    } catch (e) {
        console.error('Error fetching vault:', e);
    }
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
    btn.textContent = 'Processing...';

    try {
        const res = await fetch(`${API_BASE}/transactions`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-API-KEY': 'upismart-demo-secure-key-2026'
            },
            body: JSON.stringify(payload)
        });

        const data = await res.json();

        if (res.status === 409 && data.details && data.details.requiresConfirmation) {
            // Unusual spending rule warning!
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

        // Success!
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
                'X-API-KEY': 'upismart-demo-secure-key-2026'
            },
            body: JSON.stringify(payload)
        });
        const data = await res.json();

        if (!res.ok) {
            alert(`Vault Operation Failed: ${data.message || 'Error'}`);
            return;
        }

        closeVaultModal();
        alert(`Vault updated successfully! Reserve balance: ${formatCurrency(data.reserveBalance)}`);
        await loadDashboardData();
    } catch (err) {
        alert(`Vault error: ${err.message}`);
    }
}

function formatCurrency(val) {
    if (val === undefined || val === null || isNaN(val)) return '₹0.00';
    return '₹' + Number(val).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]));
}
