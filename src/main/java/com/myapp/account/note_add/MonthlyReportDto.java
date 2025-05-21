package com.myapp.account.note_add;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyReportDto {
	private String month;
	private int totalAmount;
	private int anomalyCount;
	private int overspendingCount;
	private List<CategoryStatDto> byCategory;
	private String suggestion;
}
//월간 리포트 전체를 나타내는 상위 DTO