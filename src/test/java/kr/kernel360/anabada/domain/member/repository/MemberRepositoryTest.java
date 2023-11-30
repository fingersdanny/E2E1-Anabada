package kr.kernel360.anabada.domain.member.repository;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import kr.kernel360.anabada.domain.member.entity.Member;
import kr.kernel360.anabada.global.commons.domain.SocialProvider;
import kr.kernel360.anabada.global.config.TestQueryDslConfig;
import kr.kernel360.anabada.global.utils.AgeGroupParser;

@DisplayName("회원 리포지토리 단위 테스트")
@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(TestQueryDslConfig.class)
class MemberRepositoryTest {
	//QueryDsl이 포함된 repository를 슬라이스 테스트 하고자 할 때, JPAQueryFactory를 Bean으로 등록해야한다. 이 때 test용 querydsl config를 만들어
	//@Import를 통해서 해당 설정을 불러와서 적용할 수 있다.
	@Autowired
	private MemberRepository memberRepository;
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
	@DisplayName("회원 저장 단위 테스트")
	void testSaveMember() {
	    //given

		//when
		Member savedMember = memberRepository.save(member);

		//then
		assertThat(member.getEmail()).isEqualTo(savedMember.getEmail());
		assertThat(member.getNickname()).isEqualTo(savedMember.getNickname());
	}

	@Test
	@Order(2)
	@DisplayName("아이디로 회원 조회 테스트")
	void testFindMemberById() {
	    //given
		Member savedMember = memberRepository.save(member);

	    //when
		Member foundMember = memberRepository.findById(savedMember.getId())
			.orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

		//then
		assertThat(foundMember.getId()).isEqualTo(savedMember.getId());
	}

	@Test
	@Order(3)
	@DisplayName("이메일로 회원이 존재하는지 확인 테스트")
	void testExistsByEmail() {
		//given
		Member savedMember = memberRepository.save(member);

		//when
		Boolean testBool = memberRepository.existsByEmail(savedMember.getEmail());

		//then
		assertThat(testBool).isTrue();
	}

	@Test
	@Order(4)
	@DisplayName("닉네임으로 회원이 존재하는지 테스트")
	void testExistsByNickname() {
	    //given
		Member savedMember = memberRepository.save(member);

	    //when
		Boolean testBool = memberRepository.existsByNickname(savedMember.getNickname());

	    //then
		assertThat(testBool).isTrue();
	}
}
