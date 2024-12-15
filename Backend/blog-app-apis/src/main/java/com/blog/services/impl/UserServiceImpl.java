package com.blog.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.blog.config.AppConstants;
import com.blog.entities.Role;
import com.blog.entities.User;
import com.blog.exceptions.ResourceNotFoundException;
import com.blog.payloads.UserDto;
import com.blog.repositories.RoleRepo;
import com.blog.repositories.UserRepo;
import com.blog.services.UserService;

@Service
public class UserServiceImpl implements UserService {

	
	@Autowired
    private UserRepo userRepo;
	
	@Autowired
	private ModelMapper modelmapper;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private RoleRepo roleRepo;
	
	@Override
    public UserDto createUser(UserDto userDto) {

        User user = this.dtoToUser(userDto);
        User savedUser = this.userRepo.save(user);
        return this.userToDto(savedUser);
    }

	@Override
    public UserDto updateUser(UserDto userDto, Integer userId) {
        User user = this.userRepo.findById(userId).orElseThrow(()->new ResourceNotFoundException("User","Id ", userId));
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setAbout(userDto.getAbout());

        User updatedUser = this.userRepo.save(user);
        UserDto userDto1 = this.userToDto(updatedUser);
        return userDto1;
    }

	@Override
    public UserDto getUserById(Integer userId) {
        User user = this.userRepo.findById(userId).orElseThrow(()->new ResourceNotFoundException("User","Id ", userId));
        return this.userToDto(user);
    }
	
	@Override
    public List<UserDto> getAllUsers() {
        List<User> users = this.userRepo.findAll();
        List<UserDto> userDtos = users.stream().map(user -> this.userToDto(user)).collect(Collectors.toList());
        return userDtos;
    }


	@Override
    public void deleteUser(Integer userId) {
        User user = this.userRepo.findById(userId).orElseThrow(()->new ResourceNotFoundException("User","Id ", userId));
        this.userRepo.delete(user);
    }
	
	 private User dtoToUser(UserDto userDto){
		 //User user = new User();
	        User user = this.modelmapper.map(userDto, User.class);
//	        user.setId(userDto.getId());
//	        user.setName(userDto.getName());
//	        user.setEmail(userDto.getEmail());
//	        user.setPassword(userDto.getPassword());
//	        user.setAbout(userDto.getAbout());
	        return user;
	    }
	 
	 public UserDto userToDto(User user){
	        UserDto userDto = this.modelmapper.map(user, UserDto.class);
//		 UserDto userDto = new UserDto();
//	        userDto.setId(user.getId());
//	        userDto.setName(user.getName());
//	        userDto.setEmail(user.getEmail());
//	        userDto.setPassword(user.getPassword());
//	        userDto.setAbout(user.getAbout());
	        return userDto;
	    }
	 
	 @Override
		public UserDto registerNewUser(UserDto userDto) {

			User user = this.modelmapper.map(userDto, User.class);

			// encoded the password
			user.setPassword(this.passwordEncoder.encode(user.getPassword()));

			// roles
			Role role = this.roleRepo.findById(AppConstants.NORMAL_USER).get();

			user.getRoles().add(role);

			User newUser = this.userRepo.save(user);

			return this.modelmapper.map(newUser, UserDto.class);
		}
	
     
}
