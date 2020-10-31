package com.jbj.gateway.filter;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "auth")
public class AuthFilter implements GlobalFilter, Ordered {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);

	@Getter
	@Setter
	private List<String> skipUrls;

	@Value("${jwt.secret}")
	private String secret;

	private AntPathMatcher antPathMatcher = new AntPathMatcher();

	@Override
	public int getOrder() {
		return -100;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String url = exchange.getRequest().getURI().getPath();
		//跳过不需要验证的路径
		if(isSkipAuthUrls(url)){
			return chain.filter(exchange);
		}
		//从请求头中取出token
		String token = exchange.getRequest().getHeaders().getFirst("Authorization");
		//未携带token或token在黑名单内
		if (token == null || token.isEmpty()) {
			ServerHttpResponse originalResponse = exchange.getResponse();
			originalResponse.setStatusCode(HttpStatus.OK);
			originalResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
			byte[] response = "{\"code\": \"401\",\"msg\": \"401 Unauthorized.\"}"
					.getBytes(StandardCharsets.UTF_8);
			DataBuffer buffer = originalResponse.bufferFactory().wrap(response);
			return originalResponse.writeWith(Flux.just(buffer));
		}
		// 校验token
		if(!validateToken(token)){
			ServerHttpResponse originalResponse = exchange.getResponse();
			originalResponse.setStatusCode(HttpStatus.OK);
			originalResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
			byte[] response = "{\"code\": \"10002\",\"msg\": \"invalid token.\"}"
					.getBytes(StandardCharsets.UTF_8);
			DataBuffer buffer = originalResponse.bufferFactory().wrap(response);
			return originalResponse.writeWith(Flux.just(buffer));
		}
		//将现在的request，添加当前身份
		ServerHttpRequest mutableReq = exchange.getRequest().mutate()
				// .header("Authorization-UserName", userName)
				.build();
		ServerWebExchange mutableExchange = exchange.mutate().request(mutableReq).build();
		return chain.filter(mutableExchange);
	}

	public boolean validateToken(String authToken) {
		try {
			Jwts.parser().setSigningKey(secret).parseClaimsJws(authToken);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			LOGGER.info("Invalid JWT token.");
			LOGGER.trace("Invalid JWT token trace.", e);
		}
		return false;
	}

	public boolean isSkipAuthUrls(String url){
		for(String skipUrl : skipUrls){
			boolean flag = antPathMatcher.match(skipUrl,url);
			if(flag){
				return true;
			}
		}
		return false;
	}
}
