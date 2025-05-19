package com.myapp.account.note_add;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryStatDto {
	public String category;
	public int totalAmount;
	public int anomalyCount;
	public int overspendingCount;
}
