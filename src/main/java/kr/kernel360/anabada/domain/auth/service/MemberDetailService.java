package kr.kernel360.anabada.domain.auth.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import kr.kernel360.anabada.domain.member.entity.Member;
import kr.kernel360.anabada.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberDetailService implements UserDetailsService {
	private final MemberRepository memberRepository;
	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		return memberRepository.findOneWithAuthoritiesByEmail(username)
			.map(member -> createUser(username, member))
			.orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원 정보 입니다."));
	}

	public List<String> toList(String str) {
		return Arrays.stream(str.split(", "))
			.map(String::trim)
			.toList();
	}

	public UserDetails createUser(String username, Member member) {
		if (!member.getAccountStatus()) {
			// todo : 추후 exception 타입 변경 필요
			throw new RuntimeException("회원 정보를 찾을 수 없습니다.");
		}

		List<SimpleGrantedAuthority> grantedAuthorities = toList(member.getAuthorities())
			.stream()
			.map(authority -> new SimpleGrantedAuthority(authority))
			.collect(Collectors.toList());

		return new User(member.getEmail(), member.getPassword(), grantedAuthorities);
	}

}