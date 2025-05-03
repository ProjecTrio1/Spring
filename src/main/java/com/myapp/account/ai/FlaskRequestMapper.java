package com.myapp.account.ai;

import com.myapp.account.note_add.NoteAdd;
import com.myapp.account.user.User;

public class FlaskRequestMapper {
	public static RequestSendToFlaskDto from(NoteAdd node, User user) {
		return RequestSendToFlaskDto.builder()
				.userId(node.getId().toString())
				.gender(user.getGender())
				.ageGroup(user.getAge())
				.categoryGroup(node.getCategory())
				.amount(node.getAmount())
				.build();
	}
}
