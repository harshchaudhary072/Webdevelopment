package com.blog.controllers;

import com.blog.entities.User;
import com.blog.exceptions.ApiException;
import com.blog.payloads.JwtAuthRequest;
import com.blog.payloads.JwtAuthResponse;
import com.blog.payloads.UserDto;
import com.blog.repositories.UserRepo;
import com.blog.security.JwtTokenHelper;
import com.blog.services.UserService;

import jakarta.validation.Valid;

import java.security.Principal;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/")
public class AuthController {

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
	private UserService userService;

    @PostMapping("/login")
	public ResponseEntity<JwtAuthResponse> createToken(@RequestBody JwtAuthRequest request) throws Exception {
		this.authenticate(request.getUsername(), request.getPassword());
		UserDetails userDetails = this.userDetailsService.loadUserByUsername(request.getUsername());
		String token = this.jwtTokenHelper.generateToken(userDetails);

		JwtAuthResponse response = new JwtAuthResponse();
		response.setJwtToken(token);
		response.setUser(this.mapper.map((User) userDetails, UserDto.class));
		return new ResponseEntity<JwtAuthResponse>(response, HttpStatus.OK);
	}

    public void authenticate(String username,String password) throws Exception{
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username,password));
        }catch (DisabledException e){
            e.printStackTrace();
            throw new Exception("User Disabled "+ e.getMessage());
        }catch (BadCredentialsException e){
            e.printStackTrace();
            throw new ApiException("Invalid Credentials !!");
        }
    }
    
 // register new user api

 	@PostMapping("/register")
 	public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserDto userDto) {
 		UserDto registeredUser = this.userService.registerNewUser(userDto);
 		return new ResponseEntity<UserDto>(registeredUser, HttpStatus.CREATED);
 	}

 	// get loggedin user data
 	@Autowired
 	private UserRepo userRepo;
 	@Autowired
 	private ModelMapper mapper;

 	@GetMapping("/current-user/")
 	public ResponseEntity<UserDto> getUser(Principal principal) {
 		User user = this.userRepo.findByEmail(principal.getName()).get();
 		return new ResponseEntity<UserDto>(this.mapper.map(user, UserDto.class), HttpStatus.OK);
 	}
    

}
