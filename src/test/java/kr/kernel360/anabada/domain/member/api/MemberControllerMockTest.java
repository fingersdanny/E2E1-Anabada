package kr.kernel360.anabada.domain.member.api;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.kernel360.anabada.domain.member.dto.UpdateMemberRequest;
import kr.kernel360.anabada.domain.member.service.MemberService;

@DisplayName("회원 컨트롤러 단위 테스트")
@WebMvcTest(MemberController.class)
class MemberControllerMockTest {
	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private MemberService memberService;

	private ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@WithMockUser
	@DisplayName("회원 정보 수정 성공 시 200 ok를 반환한다.")
	void testUpdateMemberInfo() throws Exception{
	    //given
		String newNickname = "whylongface";
		String newGender = "F";
		String newBirth = "2020-01-12";

		Long memberId = 1L;

		UpdateMemberRequest request = UpdateMemberRequest.builder()
			.nickname(newNickname)
			.gender(newGender)
			.birth(newBirth)
			.build();

		given(memberService.update(any(UpdateMemberRequest.class)))
			.willReturn(memberId);

		String jsonRequest = objectMapper.writeValueAsString(request);

	    //when & then
		mockMvc.perform(put("/api/v1/members")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.characterEncoding("UTF-8")
				.content(jsonRequest))
			.andExpect(status().isOk())
			.andExpect(content().string(String.valueOf(memberId)));

	    //verify
		verify(memberService, times(1)).update(any(UpdateMemberRequest.class));
	}

	@Test
	@WithMockUser
	@DisplayName("비밀번호 수정 성공 시 200 ok를 반환한다")
	void testUpdatePassword() throws Exception {
	    //given
		String newPassword = "hashed_password";

		Map<String, String> password = new HashMap<>();
		password.put("password", newPassword);

		Long memberId = 1L;

		given(memberService.updatePassword(newPassword))
			.willReturn(memberId);

		String jsonRequest = objectMapper.writeValueAsString(password);

		//when & then
		mockMvc.perform(put("/api/v1/members/password")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.characterEncoding("UTF-8")
				.content(jsonRequest))
			.andExpect(status().isOk())
			.andExpect(content().string(String.valueOf(memberId)));

		//verify
		verify(memberService, times(1)).updatePassword(anyString());
	}
}

