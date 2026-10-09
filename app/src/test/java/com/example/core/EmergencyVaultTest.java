package com.example.core;

import com.example.core.exception.InsufficientBalanceException;
import com.example.core.exception.InvalidAmountException;
import com.example.core.exception.InvalidPinException;
import com.example.core.exception.VaultLockedException;
import com.example.core.model.VaultAuditLog;
import com.example.core.vault.EmergencyVault;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for the Emergency Vault concept.
 * Verifies separately tracked emergency reserve, withdrawal validation,
 * protection against brute-force invalid access attempts, and audit logging.
 */
public class EmergencyVaultTest {

    private EmergencyVault vault;

    @Before
    public void setUp() {
        vault = new EmergencyVault(15000.0, "1234");
    }

    @Test
    public void testInitialStateAndSeparationOfFunds() {
        assertEquals(15000.0, vault.getReserveBalance(), 0.001);
        assertFalse("Vault must be unlocked initially", vault.isLocked());
        assertEquals(0, vault.getFailedAttempts());

        List<VaultAuditLog> logs = vault.getAuditLogs();
        assertEquals(1, logs.size());
        assertEquals(VaultAuditLog.ActionType.DEPOSIT, logs.get(0).getActionType());
        assertTrue(logs.get(0).isSuccessful());
    }

    @Test
    public void testDepositIncreasesReserve() throws InvalidAmountException {
        vault.deposit(5000.0);
        assertEquals(20000.0, vault.getReserveBalance(), 0.001);

        List<VaultAuditLog> logs = vault.getAuditLogs();
        assertEquals(2, logs.size());
        assertEquals(5000.0, logs.get(1).getAmount(), 0.001);
    }

    @Test(expected = InvalidAmountException.class)
    public void testDepositNegativeThrowsException() throws InvalidAmountException {
        vault.deposit(-100.0);
    }

    @Test
    public void testSuccessfulWithdrawalWithPin() throws Exception {
        double withdrawn = vault.withdraw(3000.0, "1234");
        assertEquals(3000.0, withdrawn, 0.001);
        assertEquals(12000.0, vault.getReserveBalance(), 0.001);

        List<VaultAuditLog> logs = vault.getAuditLogs();
        assertEquals(2, logs.size());
        assertEquals(VaultAuditLog.ActionType.WITHDRAWAL, logs.get(1).getActionType());
        assertTrue(logs.get(1).isSuccessful());
    }

    @Test
    public void testWithdrawExceedingReserveThrowsInsufficientBalanceException() {
        try {
            vault.withdraw(20000.0, "1234"); // only 15,000 available
            fail("Must throw InsufficientBalanceException");
        } catch (InsufficientBalanceException e) {
            assertEquals(20000.0, e.getAttemptedAmount(), 0.001);
            assertEquals(15000.0, e.getAvailableBalance(), 0.001);
            assertEquals(15000.0, vault.getReserveBalance(), 0.001); // Balance unchanged
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getClass().getName());
        }
    }

    @Test
    public void testInvalidPinDecrementsAttempts() {
        try {
            vault.withdraw(1000.0, "0000"); // Wrong PIN
            fail("Must throw InvalidPinException");
        } catch (InvalidPinException e) {
            assertEquals(2, e.getRemainingAttempts());
            assertEquals(1, vault.getFailedAttempts());
            assertFalse(vault.isLocked());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getClass().getName());
        }
    }

    @Test
    public void testThreeInvalidAttemptsTriggersVaultLockout() {
        // Attempt 1: Wrong PIN
        try { vault.withdraw(500.0, "1111"); } catch (InvalidPinException ignored) {}
        assertFalse(vault.isLocked());
        assertEquals(1, vault.getFailedAttempts());

        // Attempt 2: Wrong PIN
        try { vault.withdraw(500.0, "2222"); } catch (InvalidPinException ignored) {}
        assertFalse(vault.isLocked());
        assertEquals(2, vault.getFailedAttempts());

        // Attempt 3: Wrong PIN -> Lockout!
        try {
            vault.withdraw(500.0, "3333");
            fail("Must throw VaultLockedException on third consecutive failure");
        } catch (VaultLockedException e) {
            assertTrue("Vault must be in locked state", vault.isLocked());
            assertTrue(e.getMessage().contains("locked"));
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getClass().getName());
        }

        // Subsequent attempt with even the CORRECT PIN must be rejected while locked
        try {
            vault.withdraw(500.0, "1234");
            fail("Must reject even valid PIN during security lockout");
        } catch (VaultLockedException e) {
            assertTrue(vault.isLocked());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getClass().getName());
        }
    }

    @Test
    public void testManualLockAndUnlock() throws Exception {
        vault.lock();
        assertTrue(vault.isLocked());

        // Unlock with correct PIN
        boolean unlocked = vault.unlock("1234");
        assertTrue(unlocked);
        assertFalse(vault.isLocked());
    }
}
