package com.myapp.account.user;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
	private final UserRepository userRepository;
	
	public User createUser(String username, String email, String password, String gender, Integer age) {
		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setAge(age);
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		user.setPassword(passwordEncoder.encode(password));
		user.setGender(gender);
		this.userRepository.save(user);
		return user;
	}

}
