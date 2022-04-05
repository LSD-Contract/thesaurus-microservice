package com.lsd.thesaurus.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@WebFilter
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter implements Filter {


	/**
	 * This method filters every request and response going through api calls
	 * @param ServletRequest req
	 * @param ServletResponse res
	 * @param FilterChain chain
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

	    HttpServletRequest request = (HttpServletRequest) req;
	    HttpServletResponse response = (HttpServletResponse) res;

	    response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
	    response.setHeader("Access-Control-Allow-Credentials", "true");
	    response.setHeader("Access-Control-Allow-Methods", "POST,PUT,GET,OPTIONS,DELETE,HEAD");
	    response.setHeader("Access-Control-Max-Age", "3600");
	    response.setHeader("Access-Control-Allow-Headers", "Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization,X-Custom-Header,Access-Control-Allow-Headers");
	    response.setHeader("Set-Cookie", "key=value; HttpOnly; SameSite=strict");

	    //logger.debug(1, this.getClass(), "Request method : "+request.getMethod()+" from Origin : "+request.getHeader("Origin"));
        if (request.getMethod().equals(HttpMethod.OPTIONS.name())) {
        	response.setStatus(HttpStatus.NO_CONTENT.value());
        } else {
            chain.doFilter(request, response);
        }
	}

	@Override
	public void init(FilterConfig filterConfig) {

	}

	@Override
	public void destroy() {

	}

}
