package com.myapp.account.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RequestMapping("/user")
@RequiredArgsConstructor
@Controller
public class UserController {
	private final UserRepository userRepository;
	private final UserService userService;
	
	@PostMapping("/signup")
	public ResponseEntity<String> signup(@RequestBody @Valid UserCreate userCreate){
//		System.out.println("회원가입 요청 도착 "+ userCreate.getEmail());
//		System.out.println("나이 확인: " + userCreate.getAge());
		if(!userCreate.getPassword1().equals(userCreate.getPassword2())) {
			return ResponseEntity.badRequest().body("2개의 비밀번호가 일치하지 않습니다.");//400
		}
		try {
			userService.createUser(userCreate.getUsername(), userCreate.getEmail(), userCreate.getPassword1(),userCreate.getGender(),userCreate.getAge());
			return ResponseEntity.ok("회원가입 성공");
		}catch(DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 사용자입니다.");//409
		}catch(Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원가입 실패"+e.getMessage());//500
		}
	}
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody User user, BindingResult bindingResult){
		Optional<User> existUser = userRepository.findByEmail(user.getEmail());
		Map<String, String> response = new HashMap<>();
		if(bindingResult.hasErrors()) {
			response.put("message", bindingResult.getFieldError().getDefaultMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		if(existUser.isPresent()) {
			User user1 = existUser.get();
			if(passwordEncoder.matches(user.getPassword(),user1.getPassword())) {
				Map<String, Object> responses = new HashMap<>();
				responses.put("message", "로그인 성공");
				
				Map<String, Object> currentUser = new HashMap<>();
				currentUser.put("id", user1.getId());
				//currentUser.put("username", user1.getUsername());
				currentUser.put("email", user1.getEmail());
				
				responses.put("user", currentUser);
				return ResponseEntity.ok(responses);
			}else {
				response.put("message", "비밀번호가 올바르지 않습니다.");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);//401
			}
		}else{
			response.put("message", "존재하지 않는 사용자입니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);//404
		}
	}
}
