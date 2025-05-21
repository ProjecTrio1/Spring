package com.myapp.account.note_add;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackDto {
	private Long noteId;
	private boolean agree;
}
//사용자 피드백 전송용