package kr.kernel360.anabada.domain.auth.api;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.kernel360.anabada.domain.auth.dto.LoginRequest;
import kr.kernel360.anabada.domain.auth.dto.LoginResponse;
import kr.kernel360.anabada.domain.auth.dto.SignUpRequest;
import kr.kernel360.anabada.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/v1/auth/authenticate")
	public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginRequest loginRequest) {
		LoginResponse loginResponse = authService.authenticate(loginRequest);
		return ResponseEntity.ok().body(loginResponse);
	}

	@GetMapping("/v1/authorize")
	public HttpStatus authorize() {
		return HttpStatus.OK;
	}

	@PostMapping("/v1/auth/isEmailUnique")
	public ResponseEntity isEmailUnique(@RequestBody SignUpRequest signUpRequest) {;
		authService.isEmailUnique(signUpRequest.getEmail());
		return ResponseEntity.ok().build();
	}

	@PostMapping("/v1/auth/isNicknameUnique")
	public ResponseEntity isNicknameUnique(@RequestBody SignUpRequest signUpRequest) {
		authService.isNickname(signUpRequest.getNickname());
		return ResponseEntity.ok().build();
	}

	@PostMapping("/v1/auth/signUp")
	public ResponseEntity<Long> signUp(@RequestBody SignUpRequest signUpRequest) {
		Long savedMemberId = authService.signUp(signUpRequest);
		URI uri =  URI.create("/api//v1/auth/signUp"+savedMemberId);
		return ResponseEntity.created(uri).build();
	}
}