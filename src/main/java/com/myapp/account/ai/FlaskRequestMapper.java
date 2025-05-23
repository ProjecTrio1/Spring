package com.myapp.account.ai;

import com.myapp.account.note_add.NoteAdd;
import com.myapp.account.user.User;

public class FlaskRequestMapper {
	public static RequestSendToFlaskDto from(NoteAdd node, User user) {
		int genderCode = "M".equalsIgnoreCase(user.getGender()) ? 1:0;
		return RequestSendToFlaskDto.builder()
				.userId(user.getId().toString())
				.gender(genderCode)
				.ageGroup(user.getAge())
				.categoryGroup(node.getCategory())
				.amount(node.getAmount())
				.isIncome(node.getIsIncome())
				.build();
	}
}
