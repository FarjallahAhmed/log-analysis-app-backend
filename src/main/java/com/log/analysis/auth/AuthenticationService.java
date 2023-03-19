package com.log.analysis.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.log.analysis.security.config.JwtService;
import com.log.analysis.security.token.Token;
import com.log.analysis.security.token.TokenRepository;
import com.log.analysis.security.token.TokenType;
import com.log.analysis.security.user.Role;
import com.log.analysis.security.user.User;
import com.log.analysis.security.user.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

	 @Autowired
	  private  UserRepository repository;
	 @Autowired
	  private  TokenRepository tokenRepository;
	 @Autowired
	  private  PasswordEncoder passwordEncoder;
	 @Autowired
	  private  JwtService jwtService;
	 @Autowired
	  private  AuthenticationManager authenticationManager;
	  
	  public AuthenticationResponse register(RegisterRequest request) {
		  
	    var user = User.builder()
	        .firstname(request.getFirstname())
	        .lastname(request.getLastname())
	        .email(request.getEmail())
	        .password(passwordEncoder.encode(request.getPassword()))
	        .role(Role.USER)
	        .build();
	    var savedUser = repository.save(user);
	    var jwtToken = jwtService.generateToken(user);
	    saveUserToken(savedUser, jwtToken);
	    return AuthenticationResponse.builder()
	        .token(jwtToken)
	        .build();
	  }

	  public AuthenticationResponse authenticate(AuthenticationRequest request) {
	    authenticationManager.authenticate(
	        new UsernamePasswordAuthenticationToken(
	            request.getEmail(),
	            request.getPassword()
	        )
	    );
	    var user = repository.findByEmail(request.getEmail())
	        .orElseThrow();
	    var jwtToken = jwtService.generateToken(user);
	    revokeAllUserTokens(user);
	    saveUserToken(user, jwtToken);
	    return AuthenticationResponse.builder()
	        .token(jwtToken)
	        .build();
	  }

	  private void saveUserToken(User user, String jwtToken) {
	    var token = Token.builder()
	        .user(user)
	        .token(jwtToken)
	        .tokenType(TokenType.BEARER)
	        .expired(false)
	        .revoked(false)
	        .build();
	    tokenRepository.save(token);
	  }

	  private void revokeAllUserTokens(User user) {
	    var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
	    if (validUserTokens.isEmpty())
	      return;
	    validUserTokens.forEach(token -> {
	      token.setExpired(true);
	      token.setRevoked(true);
	    });
	    tokenRepository.saveAll(validUserTokens);
	  }
}
