package com.log.analysis.security.token;



import com.log.analysis.security.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Token {
	
	  @Id
	  @GeneratedValue
	  public Integer id;
	
	  @Column(unique = true)
	  public String token;
	
	  @Enumerated(EnumType.STRING)
	  public TokenType tokenType = TokenType.BEARER;
	
	  public boolean revoked;
	
	  public boolean expired;
	
	  @ManyToOne
	  @JoinColumn(name = "user_id")
	  public User user;

	public boolean isRevoked() {
		return revoked;
	}

	public void setRevoked(boolean revoked) {
		this.revoked = revoked;
	}

	public boolean isExpired() {
		return expired;
	}

	public void setExpired(boolean expired) {
		this.expired = expired;
	}
	  
	  

}
