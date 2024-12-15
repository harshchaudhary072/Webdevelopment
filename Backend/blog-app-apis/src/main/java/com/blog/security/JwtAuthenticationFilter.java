package com.blog.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //get jwt
        //bearer
        //validate

        final String requestTokenHeader = request.getHeader("Authorization");
        
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while(headerNames.hasMoreElements())
		{
			System.out.println(headerNames.nextElement());
		}

        System.out.println(requestTokenHeader);
        String username=null;
        String jwtToken=null;
        //null and format
        if (requestTokenHeader!=null && requestTokenHeader.startsWith("Bearer "))
        {
            jwtToken=requestTokenHeader.substring(7);
            try{
                username = this.jwtTokenHelper.getUsernameFromToken(jwtToken);
            }catch (ExpiredJwtException e){
                e.printStackTrace();
                System.out.println("jwt token has expired");
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
                System.out.println("Unable to get Jwt token");
            }catch (MalformedJwtException e) {
            	System.out.println("invalid jwt");
            }


            //security validated
            if (username != null && SecurityContextHolder.getContext().getAuthentication()==null)
            {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (this.jwtTokenHelper.validateToken(jwtToken, userDetails)){
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }

            }
            else {
                System.out.println("Token is not valid...");
            }

        }else {
        	System.out.println("username is null or context is null");
        }
        filterChain.doFilter(request,response);

    }
}
