package kr.kernel360.anabada.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import kr.kernel360.anabada.domain.member.dto.UpdateMemberRequest;
import kr.kernel360.anabada.domain.member.entity.Member;
import kr.kernel360.anabada.domain.member.repository.MemberRepository;
import kr.kernel360.anabada.global.commons.domain.SocialProvider;
import kr.kernel360.anabada.global.utils.AgeGroupParser;

@DisplayName("회원 서비스 통합 테스트")
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemberServiceTest {
	// todo : test 순서 바뀌면 동작 제대로 안함 (삭제 메서드 테스트에서 roll back이 안되고 있음)
	// 그래서 transactional을 testRemove에 적용하면 테스트 통과를 못함 (soft delete 하지 않고 쿼리가 해당 회원을 찾는데서 갑자기 끝남)
	// 지금 이 상태면 test db에 저런 회원의 데이터가 남아있는게 아닌가..? 추후에 확인이 필요함
	@Autowired
	private MemberService memberService;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	private Member member;

	private static Member createMember() {
		return Member.builder()
			.email("ad2d@naver.com")
			.nickname("iwanttogohome")
			.password("123412")
			.ageGroup(AgeGroupParser.birthToAgeGroup("1991-10-10"))
			.authorities("USER_ROLE")
			.gender("M")
			.birth("1991-10-10")
			.socialProvider(SocialProvider.LOCAL)
			.accountStatus(true)
			.build();
	}

	@BeforeEach
	void setUp() {
		member = createMember();
	}

	@Test
	@Order(1)
	@WithMockUser(username = "ad2d@naver.com")
	@DisplayName("회원 수정 정보를 입력하면, 회원 정보를 수정하고 해당 회원의 아이디를 반환한다.")
	@Transactional
	void testUpdateMember() {
		//given
		String newNickname = "whylongface";
		String newGender = "F";
		String newBirth = "2020-01-12";

		Member savedMember = memberRepository.save(member);

		UpdateMemberRequest request = UpdateMemberRequest.builder()
			.nickname(newNickname)
			.gender(newGender)
			.birth(newBirth)
			.build();

		//when
		Long updatedId = memberService.update(request);
		Member foundMember = memberRepository.findById(updatedId).get();

		//then
		assertThat(updatedId).isEqualTo(savedMember.getId());
		assertThat(foundMember.getNickname()).isEqualTo(newNickname);
		assertThat(foundMember.getGender()).isEqualTo(newGender);
		assertThat(foundMember.getBirth()).isEqualTo(newBirth);
	}
	@Test
	@Order(2)
	@WithMockUser(username = "ad2d@naver.com", password = "123412")
	@DisplayName("회원이 비밀번호를 수정하면, 비밀번호를 수정하고 해당 회원의 아이디를 반환한다.")
	@Transactional
	void testUpdatePassword(){
	    //given
		String newPassword = "hashed_password";

		Member savedMember = memberRepository.save(member);

	    //when
		Long updatedId = memberService.updatePassword(newPassword);
		Member foundMember = memberRepository.findById(updatedId).get();

		//then
		assertEquals(updatedId, savedMember.getId());
		assertThat(updatedId).isEqualTo(savedMember.getId());
		assertThat(passwordEncoder.matches(newPassword, foundMember.getPassword())).isTrue();
	}

	@Test
	@Order(3)
	@WithMockUser(username = "ad2d@naver.com")
	@DisplayName("회원 계정 비활성화 테스트")
	void testRemove() {
		//given
		Member savedMember = memberRepository.save(member);

		//when
		Long removedId = memberService.remove(savedMember.getId());
		Member foundMember = memberRepository.findById(removedId).get();

		//then
		assertThat(removedId).isEqualTo(foundMember.getId());
		assertThat(foundMember.getAccountStatus()).isFalse();
	}
}
