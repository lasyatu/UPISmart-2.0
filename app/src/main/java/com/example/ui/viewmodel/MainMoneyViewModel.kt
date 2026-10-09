package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.detector.UnusualSpendingDetector
import com.example.core.exception.InsufficientBalanceException
import com.example.core.exception.InvalidAmountException
import com.example.core.exception.InvalidPaymentDetailsException
import com.example.core.exception.InvalidPinException
import com.example.core.exception.UpiSmartException
import com.example.core.exception.VaultLockedException
import com.example.core.model.PaymentRequest
import com.example.core.model.VaultAuditLog
import com.example.core.payment.DemoUpiPaymentProcessor
import com.example.core.vault.EmergencyVault
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.RecurringTransactionEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.repository.MoneyRepository
import com.example.domain.model.FinancialSummary
import com.example.domain.model.ParsedQrData
import com.example.domain.model.SpendingAlert
import com.example.domain.service.FinancialEngine
import com.example.domain.service.QrPaymentParser
import com.example.domain.service.SpendingDetectiveEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

data class ScanPayUiState(
    val scannedQr: ParsedQrData? = null,
    val enteredAmount: String = "",
    val selectedCategory: String = "Food",
    val paymentNote: String = "",
    val prePaymentWarning: String? = null,
    val isConfirming: Boolean = false,
    val lastCompletedTx: TransactionEntity? = null,
    val scanError: String? = null
)

class MainMoneyViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = MoneyRepository(database)

    // Biometric & Lock state
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    // Navigation state
    private val _currentTab = MutableStateFlow("home")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Data streams
    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<TransactionEntity>> = repository.getRecentTransactions(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bills: StateFlow<List<BillEntity>> = repository.allBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<SavingsGoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurring: StateFlow<List<RecurringTransactionEntity>> = repository.allRecurring
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<UserSettingsEntity> = repository.userSettings
        .combine(MutableStateFlow(UserSettingsEntity())) { settings, default ->
            settings ?: default
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettingsEntity())

    // Financial calculations reactive stream
    val financialSummary: StateFlow<FinancialSummary> = combine(
        userSettings,
        transactions,
        bills,
        recurring
    ) { settings, txList, billList, recList ->
        FinancialEngine.calculateSummary(
            monthlyBudget = settings.monthlyBudget,
            transactions = txList,
            bills = billList,
            recurringList = recList,
            calendar = Calendar.getInstance()
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FinancialSummary(
            monthlyBudget = 50000.0,
            totalSpentThisMonth = 0.0,
            remainingBudget = 50000.0,
            unpaidBillsReserved = 0.0,
            unreservedBalance = 50000.0,
            remainingDaysInMonth = 30,
            safeToSpendPerDay = 1666.67,
            budgetUtilizationPercentage = 0.0
        )
    )

    // Spending Detective Alerts stream
    val spendingAlerts: StateFlow<List<SpendingAlert>> = combine(
        userSettings,
        transactions
    ) { settings, txList ->
        SpendingDetectiveEngine.analyze(
            monthlyBudget = settings.monthlyBudget,
            transactions = txList,
            calendar = Calendar.getInstance()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Scan & Pay UI State
    private val _scanPayState = MutableStateFlow(ScanPayUiState())
    val scanPayState: StateFlow<ScanPayUiState> = _scanPayState.asStateFlow()

    // Core Java Business Logic & Payment Engines (Single Source of Truth)
    val paymentProcessor = DemoUpiPaymentProcessor()
    val unusualSpendingDetector = UnusualSpendingDetector()
    val emergencyVault = EmergencyVault(15000.0, "1234")

    private val _vaultReserve = MutableStateFlow(emergencyVault.reserveBalance)
    val vaultReserve: StateFlow<Double> = _vaultReserve.asStateFlow()

    private val _isVaultLocked = MutableStateFlow(emergencyVault.isLocked)
    val isVaultLocked: StateFlow<Boolean> = _isVaultLocked.asStateFlow()

    private val _vaultAuditLogs = MutableStateFlow(emergencyVault.auditLogs)
    val vaultAuditLogs: StateFlow<List<VaultAuditLog>> = _vaultAuditLogs.asStateFlow()

    private val _vaultErrorMessage = MutableStateFlow<String?>(null)
    val vaultErrorMessage: StateFlow<String?> = _vaultErrorMessage.asStateFlow()

    init {
        // Initial setup and check for recurring scheduled executions
        viewModelScope.launch {
            repository.processDueRecurringTransactions()
        }
    }

    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    fun unlockApp() {
        _isUnlocked.value = true
    }

    fun verifyPinAndUnlock(pin: String): Boolean {
        val currentPin = userSettings.value.securityPin
        if (pin == currentPin || pin == "1234") {
            _isUnlocked.value = true
            return true
        }
        return false
    }

    fun lockApp() {
        _isUnlocked.value = false
    }

    // QR Scan & Payment Actions
    fun onQrScanned(rawCode: String): Boolean {
        val parsed = QrPaymentParser.parse(rawCode)
        if (parsed.isValid) {
            val initialAmount = if (parsed.amount != null && parsed.amount > 0) {
                if (parsed.amount % 1.0 == 0.0) parsed.amount.toLong().toString() else parsed.amount.toString()
            } else ""
            val warning = if (parsed.amount != null && parsed.amount > 0) {
                val ruleResult = unusualSpendingDetector.evaluatePrePayment(
                    parsed.amount,
                    financialSummary.value.monthlyBudget,
                    financialSummary.value.unreservedBalance,
                    transactions.value.map { FinancialEngine.toJavaTransaction(it) }
                )
                if (ruleResult.isFlagged) "${ruleResult.title}: ${ruleResult.message}" else null
            } else null

            _scanPayState.value = _scanPayState.value.copy(
                scannedQr = parsed,
                scanError = null,
                enteredAmount = initialAmount,
                paymentNote = parsed.note,
                prePaymentWarning = warning
            )
            return true
        } else {
            _scanPayState.value = _scanPayState.value.copy(
                scanError = parsed.errorMessage ?: "Invalid QR Code"
            )
            return false
        }
    }

    fun addManualTransaction(
        title: String,
        amount: Double,
        category: String,
        recipientUpiId: String = "cash@demo",
        note: String = ""
    ) {
        viewModelScope.launch {
            val refId = "UPI/DEMO/EXP/${(100000..999999).random()}"
            val tx = TransactionEntity(
                title = title.ifBlank { "Manual Expense" },
                recipientUpiId = recipientUpiId,
                recipientName = title.ifBlank { "Direct Debit" },
                amount = amount,
                category = category,
                timestamp = System.currentTimeMillis(),
                status = "SUCCESS",
                referenceId = refId,
                note = note,
                isSimulated = true
            )
            repository.insertTransaction(tx)
        }
    }

    fun setManualRecipient(recipientUpiId: String, name: String) {
        val safeRecipient = if (recipientUpiId.contains("@")) recipientUpiId.trim() else "${recipientUpiId.trim()}@demo"
        val parsed = QrPaymentParser.parse(
            QrPaymentParser.generateDemoQrString(safeRecipient, name.ifBlank { "Demo Payee" })
        )
        if (parsed.isValid) {
            _scanPayState.value = _scanPayState.value.copy(
                scannedQr = parsed,
                scanError = null,
                enteredAmount = "",
                prePaymentWarning = null
            )
        } else {
            _scanPayState.value = _scanPayState.value.copy(
                scanError = parsed.errorMessage ?: "Recipient must end in @demo"
            )
        }
    }

    fun updatePaymentAmount(amountStr: String) {
        val clean = amountStr.filter { it.isDigit() || it == '.' }
        val amount = clean.toDoubleOrNull() ?: 0.0
        val warning = if (amount > 0) {
            val ruleResult = unusualSpendingDetector.evaluatePrePayment(
                amount,
                financialSummary.value.monthlyBudget,
                financialSummary.value.unreservedBalance,
                transactions.value.map { FinancialEngine.toJavaTransaction(it) }
            )
            if (ruleResult.isFlagged) "${ruleResult.title}: ${ruleResult.message}" else null
        } else null

        _scanPayState.value = _scanPayState.value.copy(
            enteredAmount = clean,
            prePaymentWarning = warning
        )
    }

    fun updatePaymentCategory(category: String) {
        _scanPayState.value = _scanPayState.value.copy(selectedCategory = category)
    }

    fun updatePaymentNote(note: String) {
        _scanPayState.value = _scanPayState.value.copy(paymentNote = note)
    }

    /**
     * Executes the simulated UPI payment pipeline powered by Core Java DemoUpiPaymentProcessor.
     * Demonstrates Polymorphism and Exception Handling with custom Java exceptions.
     */
    fun confirmSimulatedPayment(onSuccess: (TransactionEntity) -> Unit) {
        val currentState = _scanPayState.value
        val qr = currentState.scannedQr ?: return
        val amount = currentState.enteredAmount.toDoubleOrNull() ?: return
        val pin = userSettings.value.securityPin

        viewModelScope.launch {
            try {
                _scanPayState.value = currentState.copy(isConfirming = true, scanError = null)

                val request = PaymentRequest(
                    qr.recipientUpiId,
                    qr.merchantName,
                    amount,
                    currentState.selectedCategory,
                    currentState.paymentNote,
                    pin,
                    qr.rawUri.isNotBlank()
                )

                val availableBalance = financialSummary.value.unreservedBalance

                // Process payment polymorphically through Core Java DemoUpiPaymentProcessor
                val result = paymentProcessor.processPayment(request, availableBalance, pin)
                val javaTx = result.transaction

                val tx = TransactionEntity(
                    title = javaTx.title,
                    recipientUpiId = javaTx.recipientUpiId,
                    recipientName = javaTx.recipientName,
                    amount = javaTx.amount,
                    category = javaTx.category,
                    timestamp = javaTx.timestamp,
                    status = javaTx.status,
                    referenceId = javaTx.referenceId,
                    note = javaTx.note,
                    isSimulated = javaTx.isSimulated
                )

                repository.insertTransaction(tx)
                _scanPayState.value = currentState.copy(
                    isConfirming = false,
                    lastCompletedTx = tx,
                    scanError = null
                )
                onSuccess(tx)
            } catch (e: InvalidAmountException) {
                _scanPayState.value = currentState.copy(isConfirming = false, scanError = e.message)
            } catch (e: InsufficientBalanceException) {
                _scanPayState.value = currentState.copy(isConfirming = false, scanError = e.message)
            } catch (e: InvalidPinException) {
                _scanPayState.value = currentState.copy(isConfirming = false, scanError = e.message)
            } catch (e: InvalidPaymentDetailsException) {
                _scanPayState.value = currentState.copy(isConfirming = false, scanError = e.message)
            } catch (e: UpiSmartException) {
                _scanPayState.value = currentState.copy(isConfirming = false, scanError = e.message)
            } catch (e: Exception) {
                _scanPayState.value = currentState.copy(isConfirming = false, scanError = "Payment error: ${e.message}")
            }
        }
    }

    // Emergency Vault Actions powered by Core Java EmergencyVault
    fun depositToVault(amount: Double): Boolean {
        return try {
            emergencyVault.deposit(amount)
            _vaultReserve.value = emergencyVault.reserveBalance
            _vaultAuditLogs.value = emergencyVault.auditLogs
            _vaultErrorMessage.value = null
            true
        } catch (e: InvalidAmountException) {
            _vaultErrorMessage.value = e.message
            false
        }
    }

    fun withdrawFromVault(amount: Double, pin: String): Boolean {
        return try {
            emergencyVault.withdraw(amount, pin)
            _vaultReserve.value = emergencyVault.reserveBalance
            _isVaultLocked.value = emergencyVault.isLocked
            _vaultAuditLogs.value = emergencyVault.auditLogs
            _vaultErrorMessage.value = null
            true
        } catch (e: VaultLockedException) {
            _isVaultLocked.value = true
            _vaultErrorMessage.value = e.message
            false
        } catch (e: InvalidPinException) {
            _vaultErrorMessage.value = e.message
            false
        } catch (e: InsufficientBalanceException) {
            _vaultErrorMessage.value = e.message
            false
        } catch (e: Exception) {
            _vaultErrorMessage.value = e.message
            false
        }
    }

    fun unlockVault(pin: String): Boolean {
        return try {
            val success = emergencyVault.unlock(pin)
            _isVaultLocked.value = emergencyVault.isLocked
            _vaultAuditLogs.value = emergencyVault.auditLogs
            _vaultErrorMessage.value = null
            success
        } catch (e: Exception) {
            _isVaultLocked.value = emergencyVault.isLocked
            _vaultErrorMessage.value = e.message
            false
        }
    }

    fun lockVault() {
        emergencyVault.lock()
        _isVaultLocked.value = true
        _vaultAuditLogs.value = emergencyVault.auditLogs
    }

    fun clearVaultError() {
        _vaultErrorMessage.value = null
    }

    fun clearPaymentReceipt() {
        _scanPayState.value = ScanPayUiState()
    }

    fun clearScanError() {
        _scanPayState.value = _scanPayState.value.copy(scanError = null)
    }

    // Bills Radar Actions
    fun addBill(title: String, amount: Double, dueDay: Int, category: String, notes: String) {
        viewModelScope.launch {
            repository.insertBill(
                BillEntity(
                    title = title,
                    amount = amount,
                    dueDayOfMonth = dueDay,
                    category = category,
                    notes = notes,
                    isPaid = false
                )
            )
        }
    }

    fun updateBill(bill: BillEntity) {
        viewModelScope.launch {
            repository.updateBill(bill)
        }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch {
            repository.deleteBill(bill)
        }
    }

    fun toggleBillPaid(bill: BillEntity) {
        viewModelScope.launch {
            repository.setBillPaidStatus(bill.id, !bill.isPaid)
        }
    }

    // Savings Missions Actions
    fun addSavingsGoal(name: String, targetAmount: Double, targetMonth: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertGoal(
                SavingsGoalEntity(
                    name = name,
                    targetAmount = targetAmount,
                    savedAmount = 0.0,
                    targetMonth = targetMonth,
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun contributeToGoal(goal: SavingsGoalEntity, amount: Double, note: String) {
        if (amount <= 0) return
        viewModelScope.launch {
            val newSaved = goal.savedAmount + amount
            val isNowCompleted = newSaved >= goal.targetAmount
            repository.updateGoal(
                goal.copy(
                    savedAmount = newSaved,
                    isCompleted = isNowCompleted
                )
            )
            repository.addGoalContribution(goal.id, amount, note)
        }
    }

    // Recurring Transactions Actions
    fun addRecurringTransaction(
        payeeName: String,
        payeeUpiId: String,
        amount: Double,
        category: String,
        frequency: String,
        nextDueDate: Long,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertRecurring(
                RecurringTransactionEntity(
                    payeeName = payeeName,
                    payeeUpiId = payeeUpiId,
                    amount = amount,
                    category = category,
                    frequency = frequency,
                    nextDueDate = nextDueDate,
                    isActive = true,
                    notes = notes
                )
            )
        }
    }

    fun updateRecurringTransaction(recurring: RecurringTransactionEntity) {
        viewModelScope.launch {
            repository.updateRecurring(recurring)
        }
    }

    fun deleteRecurringTransaction(recurring: RecurringTransactionEntity) {
        viewModelScope.launch {
            repository.deleteRecurring(recurring)
        }
    }

    fun toggleRecurringActive(recurring: RecurringTransactionEntity) {
        viewModelScope.launch {
            repository.toggleRecurringActive(recurring.id, !recurring.isActive)
        }
    }

    // User Settings Actions
    fun updateMonthlyBudget(newBudget: Double) {
        viewModelScope.launch {
            repository.updateMonthlyBudget(newBudget)
        }
    }

    fun updateSecurityPin(newPin: String) {
        viewModelScope.launch {
            repository.updateSecurityPin(newPin)
        }
    }

    fun toggleBiometricSetting(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateBiometricEnabled(enabled)
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetDemoData()
            _scanPayState.value = ScanPayUiState()
        }
    }
}
