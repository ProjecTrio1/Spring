package com.exapp.comsumption;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ExpenseService {
	private final ExpenseRepository expenseRepository;
	
	public ExpenseService(ExpenseRepository expenseRepository) {
		this.expenseRepository = expenseRepository;
	}
	
	public Expense saveExpense(Expense expense) {
		expense.setCreatedAt(LocalDateTime.now());
		return expenseRepository.save(expense);
	}
	
	public List<Expense> getAllExpenses(){
		return expenseRepository.findAll();
	}
}
