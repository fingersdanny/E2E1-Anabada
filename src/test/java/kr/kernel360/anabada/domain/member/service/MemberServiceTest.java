package kr.kernel360.anabada.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.transaction.annotation.Isolation;
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

	// Spring Data Jpa는 기본 구현체로 SimpleJpaRepository를 채택
	// SimpleJpaRepository는 기본적으로 CRUD 메서드에 @Transactional이 달려 있음
	// 따라서 Repository단에서 저장을 하면 당연히 commit이 되고
	// Service layer에서도 당연히 commit이 되어야 함

	// soft delete는 어떨까? delete가 실행되면 @SQLDelete에 있는 쿼리를 실행 시킴.. 그러면?
	// 원래 delete에는 @Transactional이 달려 있음

	@Autowired
	private EntityManager entityManager;
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
	@DisplayName("회원 계정 비활성화 테스트")
	@Transactional
	void testRemove() {
		//given
		Member savedMember = memberRepository.save(member);

		//when
		Long removedId = memberService.remove(savedMember.getId());
		// EntityTransaction transaction = entityManager.getTransaction();
		// System.out.println(transaction);
		entityManager.flush();

		Member foundMember = memberRepository.findById(removedId).get();

		//then
		assertThat(removedId).isEqualTo(foundMember.getId());
		assertThat(foundMember.getAccountStatus()).isFalse();
	}

	@Test
	@Order(2)
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
	@Order(3)
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
	@DisplayName("testSave")
	@WithMockUser(username = "ad2d@naver.com")
	@Transactional
	void testSave() throws Exception {
		//given
		Member savedMember = memberRepository.save(member);

		//when
		Long removedId = memberService.remove(savedMember.getId());

		entityManager.flush();

		Member foundMember = memberRepository.findByEmail("ad2d@naver.com").get();

		//then
		assertThat(foundMember).isNotNull();
	}
}
