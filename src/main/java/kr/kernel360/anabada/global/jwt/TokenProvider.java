package kr.kernel360.anabada.global.jwt;

import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.ServletRequest;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import kr.kernel360.anabada.domain.auth.dto.TokenResponse;
import kr.kernel360.anabada.domain.auth.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider implements InitializingBean {
	private Key key;
	private final String AUTHORITIES_KEY = "auth";
	@Value("${spring.security.jwt.secret}")
	private String secret;
	@Value("${spring.security.jwt.access-token-validity-in-seconds}")
	private long accessTokenValidityInSeconds;
	@Value("${spring.security.jwt.refresh-token-validity-in-seconds}")
	private long refreshTokenValidityInSeconds;
	private final RedisTemplate<Object, Object> redisTemplate;

	@Override
	public void afterPropertiesSet() throws Exception {
		byte[] keyBytes = Decoders.BASE64.decode(secret);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	public TokenResponse createToken(Authentication authentication) {
		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));
		return TokenResponse.builder()
			.accessToken(createAccessToken(authentication.getName(), authorities))
			.refreshToken(createRefreshToken(authentication.getName()))
			.build();
	}

	/** Access Token 발급 **/
	public String createAccessToken(String sub, String roles) {
		LocalDateTime localDateTime = LocalDateTime.now()
			.plusSeconds(accessTokenValidityInSeconds);
		Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
		Date expirationDate = Date.from(instant);
		return Jwts.builder()
			.setSubject(sub)
			.claim(AUTHORITIES_KEY, roles)
			.setExpiration(expirationDate)
			.signWith(key, SignatureAlgorithm.HS512)
			.setHeaderParam(Header.TYPE, Header.JWT_TYPE)
			.compact();
	}

	/** Refresh Token 발급 **/
	public String createRefreshToken(String sub) {
		LocalDateTime expirationDate = LocalDateTime.now()
			.plusSeconds(refreshTokenValidityInSeconds);

		String uuid = UUID.randomUUID().toString().replace("-", "");

		RefreshToken refreshToken = RefreshToken.builder()
			.refreshToken(uuid)
			.createdDate(LocalDateTime.now())
			.expirationDate(expirationDate)
			.email(sub)
			.build();

		String refreshTokenJson = toJsonString(refreshToken);
		redisTemplate.opsForValue().set("refreshToken:" + refreshToken.getRefreshToken(), refreshTokenJson);

		return uuid;
	}

	private String toJsonString(RefreshToken refreshToken) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			String refreshTokenJson = mapper.writeValueAsString(refreshToken);
			return refreshTokenJson;
		} catch (JsonProcessingException exception) {
			exception.printStackTrace();
			throw new IllegalArgumentException("JsonProcessingException 발생");
		}
	}

	/** 인증 정보 조회 **/
	public Authentication getAuthentication(String token) {
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(key)
			.build()
			.parseClaimsJws(token)
			.getBody();

		List<? extends GrantedAuthority> authorities = Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
			.map(SimpleGrantedAuthority::new)
			.toList();

		User principal = new User(claims.getSubject(), "", authorities);

		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}

	/** 토큰 유효성 검증을 수행 **/
	public boolean validateToken(String token, ServletRequest request) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (SecurityException | MalformedJwtException e) {
			// log.info("잘못된 JWT 서명입니다");
			throw new IllegalArgumentException("잘못된 JWT 서명입니다"); // todo : 추후 Exception 변경
		} catch (ExpiredJwtException e) {
			// log.info("만료된 JWT 토큰입니다");
			throw new IllegalArgumentException("만료된 JWT 토큰입니다"); // todo : 추후 Exception 변경
		} catch (UnsupportedJwtException e) {
			// log.info("지원하지 않는 JWT 토큰입니다");
			throw new IllegalArgumentException("지원하지 않는 JWT 토큰입니다"); // todo : 추후 Exception 변경
		} catch (IllegalArgumentException e) {
			// log.info("잘못된 JWT 토큰입니다");
			throw new IllegalArgumentException("잘못된 JWT 토큰입니다"); // todo : 추후 Exception 변경
		}
	}
}