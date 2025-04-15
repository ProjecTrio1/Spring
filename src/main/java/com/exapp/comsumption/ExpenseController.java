package com.exapp.comsumption;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {
	private final ExpenseRepository expenseRepository;
	
	public ExpenseController(ExpenseRepository expenseRepository) {
		this.expenseRepository = expenseRepository;
	}
	
	@PostMapping("/add")
	public Expense save(@RequestBody Expense expense) {
		return expenseRepository.save(expense);
	}
	
	@GetMapping("/all")
	public List<Expense> findAll(){
		return expenseRepository.findAll();
	}

}
