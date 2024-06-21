package com.endava.expensesmanager.model.mapper;

import com.endava.expensesmanager.model.dto.ExpenseDto;
import com.endava.expensesmanager.model.entity.*;
import com.endava.expensesmanager.model.entity.Category;
import com.endava.expensesmanager.model.entity.Currency;
import com.endava.expensesmanager.model.entity.Expense;
import com.endava.expensesmanager.model.entity.User;

public class ExpenseMapper {

    public static Expense toExpense(ExpenseDto expenseDto) {
        Expense expense = new Expense();
        expense.setDescription(expenseDto.getDescription());
        expense.setExpenseDate(expenseDto.getExpenseDate());
        expense.setAmount(expenseDto.getAmount());

        User user = new User();
        user.setUserId(expenseDto.getUserId());
        expense.setUser(user);

        expense.setCategory(expenseDto.getCategory());
        expense.setCurrency(expenseDto.getCurrency());

        if (expenseDto.getDocumentId() != null) {
            Document document = new Document();
            document.setDocumentId(expenseDto.getDocumentId());
            expense.setDocument(document);
        }

        return expense;
    }

    public static ExpenseDto toDto(Expense expense) {
        ExpenseDto expenseDto = new ExpenseDto();
        expenseDto.setDescription(expense.getDescription());
        expenseDto.setExpenseDate(expense.getExpenseDate());
        expenseDto.setAmount(expense.getAmount());
        expenseDto.setUserId(expense.getUser().getUserId());
        expenseDto.setDocumentId(expense.getDocument().getDocumentId());
        expenseDto.setCategory(expense.getCategory());
        expenseDto.setCurrency(expense.getCurrency());

        return expenseDto;
    }

    public static Expense toUpdatedExpense(Expense existingExpense, ExpenseDto expenseDto, User user, Category category, Currency currency) {

        existingExpense.setDescription(expenseDto.getDescription());
        existingExpense.setAmount(expenseDto.getAmount());
        existingExpense.setExpenseDate(expenseDto.getExpenseDate());

        existingExpense.setUser(user);
        existingExpense.setCategory(category);
        existingExpense.setCurrency(currency);

        return existingExpense;
    }
}
