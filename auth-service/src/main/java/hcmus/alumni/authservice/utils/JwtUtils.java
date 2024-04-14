package hcmus.alumni.authservice.utils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import hcmus.alumni.authservice.model.UserModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
	public static final String SECRET = "givepraiseforhehasnoequalworshiphimagodhasbeenbornuntotheworldcowerinfearhewillnotforgiveanyvicedevoteyourselfyourfateisoverwritten";
	private final long expirationTime = 259200000L; // 3 days
	
	public boolean validateToken(final String token) {
		Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
        try {
        	Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            System.err.println("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
        	System.err.println("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
        	System.err.println("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
        	System.err.println("JWT claims string is empty.");
        }
        return false;
	}

	public String generateToken(UserModel user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("fullName",	user.getFullName());
		claims.put("roles", user.getRolesName());
		
		return Jwts.builder().setClaims(claims).setSubject(user.getId()).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expirationTime))
				.signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
	}

	private Key getSignKey() {
		byte[] keyBytes = Decoders.BASE64.decode(SECRET);
		return Keys.hmacShaKeyFor(keyBytes);
	}
	
	public Claims getUserIdFromJwt(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
	}
}