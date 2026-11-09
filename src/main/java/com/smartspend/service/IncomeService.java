package com.smartspend.service;

import com.smartspend.dao.IncomeDao;
import com.smartspend.model.Income;
import com.smartspend.util.SessionManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service class handling Income business logic.
 * Demonstrates:
 * 1. Business rules validation
 * 2. DAO delegation
 * 3. Monthly Income aggregation
 */
public class IncomeService {

    private final IncomeDao incomeDao;

    public IncomeService() {
        this.incomeDao = new IncomeDao();
    }

    public IncomeService(IncomeDao incomeDao) {
        this.incomeDao = incomeDao;
    }

    /**
     * Adds a new income record.
     */
    public boolean addIncome(double amount, String source, String description, LocalDate date) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId <= 0) {
            throw new IllegalStateException("No active user session.");
        }
        validateIncomeData(amount, source, date);

        Income income = new Income(userId, amount, source, description, date);
        return incomeDao.save(income);
    }

    /**
     * Updates an existing income record.
     */
    public boolean updateIncome(int incomeId, double amount, String source, String description, LocalDate date) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId <= 0) {
            throw new IllegalStateException("No active user session.");
        }
        validateIncomeData(amount, source, date);

        Income income = new Income(incomeId, userId, amount, source, description, date);
        return incomeDao.update(income);
    }

    /**
     * Deletes an income record by ID.
     */
    public boolean deleteIncome(int incomeId) {
        return incomeDao.delete(incomeId);
    }

    /**
     * Retrieves all income records for the current user.
     */
    public List<Income> getAllIncome() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        return incomeDao.findAllByUserId(userId);
    }

    /**
     * Search and filter income records.
     */
    public List<Income> filterIncome(String keyword, String source, LocalDate startDate, LocalDate endDate) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        return incomeDao.searchAndFilter(userId, keyword, source, startDate, endDate);
    }

    /**
     * Calculates total income for a specific month.
     */
    public double getTotalIncomeForMonth(String yearMonth) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        return incomeDao.getTotalIncomeForMonth(userId, yearMonth);
    }

    /**
     * Calculates total income of all time.
     */
    public double getTotalIncomeAllTime() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        return incomeDao.getTotalIncomeAllTime(userId);
    }

    /**
     * Retrieves recent income records.
     */
    public List<Income> getRecentIncome(int limit) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        return incomeDao.getRecentIncome(userId, limit);
    }

    /**
     * Retrieves monthly totals for comparison.
     */
    public Map<String, Double> getMonthlyTotals(int monthsCount) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        return incomeDao.getMonthlyTotalsForPastMonths(userId, monthsCount);
    }

    private void validateIncomeData(double amount, String source, LocalDate date) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Income amount must be greater than zero.");
        }
        if (source == null || source.trim().isEmpty()) {
            throw new IllegalArgumentException("Please select an income source.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Please select a valid date.");
        }
    }
}
