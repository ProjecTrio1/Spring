package com.myapp.account.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreate {
	@Size(min = 3, max = 25)
	@NotEmpty(message = "사용자 ID는 필수 항목입니다.")
	private String username;
	
	@NotEmpty(message = "성별은 필수 항목입니다.")
	private String gender;
	
	private Integer age;
	
	@Size(min = 8, max = 20)
	@NotEmpty(message = "비밀번호는 필수 항목입니다.")
	private String password1;
	
	@NotEmpty(message = "비밀번호 확인은 필수 항목입니다.")
	private String password2;
	
	@NotEmpty(message = "이메일은 필수 항목힙니다.")
	@Email(message= "이메일 형식이 아닙니다.")
	private String email;
}
