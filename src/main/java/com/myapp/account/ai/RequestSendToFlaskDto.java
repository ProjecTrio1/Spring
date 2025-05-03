package com.myapp.account.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestSendToFlaskDto {
	private String userId;
	private String gender;
	
	@JsonProperty("age")
	private int ageGroup;
	
	@JsonProperty("category_group")
	private String categoryGroup;
	
	@JsonProperty("amt")
	private int amount;
}
