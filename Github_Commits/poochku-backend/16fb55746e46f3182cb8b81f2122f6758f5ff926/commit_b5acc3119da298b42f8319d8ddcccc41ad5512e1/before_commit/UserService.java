package com.puchku.pet.service;

import com.puchku.pet.exceptions.BadRequestException;
import com.puchku.pet.exceptions.NotFoundException;
import com.puchku.pet.model.UserDto;
import com.puchku.pet.model.entities.SellerEntity;
import com.puchku.pet.repository.SellerRepository;
import com.puchku.pet.util.CommonUtils;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseEntity<UserDto> createUserAccount(UserDto userDto) {
        if(userDto!=null && (StringUtils.isEmpty(userDto.getPhoneNo()) || StringUtils.isEmpty(userDto.getEmail()))) {
            throw new BadRequestException("Missing phone no. or email");
        }
        SellerEntity sellerEntity = new SellerEntity();
        createSellerEntityFromSellerDto(sellerEntity, userDto);

        sellerEntity = sellerRepository.save(sellerEntity);

        createSellerDtoFromSellerEntity(sellerEntity, userDto);
        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    private void createSellerEntityFromSellerDto(SellerEntity sellerEntity, UserDto userDto) {
        if(userDto.getUserId()!=null && sellerEntity.getSellerId()!=0){
            sellerEntity.setSellerId(userDto.getUserId());
        }
        if(StringUtils.isNotBlank(userDto.getEmail())){
            sellerEntity.setEmail(userDto.getEmail());
        }
        if(StringUtils.isNotBlank(userDto.getfName())){
            sellerEntity.setfName(userDto.getfName());
        }
        if(StringUtils.isNotBlank(userDto.getlName())) {
            sellerEntity.setlName(userDto.getlName());
        }
        if(StringUtils.isNotBlank(userDto.getPhoneNo())) {
            sellerEntity.setPhoneNo(userDto.getPhoneNo());
        }
        if(StringUtils.isNotBlank(userDto.getRole())) {
            sellerEntity.setRoles(List.of(userDto.getRole()));
        }

        if(StringUtils.isNotBlank(userDto.getPassword())) {
            sellerEntity.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        sellerEntity.setLastLogin(OffsetDateTime.now());
        sellerEntity.setCrtTs(OffsetDateTime.now());
    }

    public ResponseEntity<UserDto> getUserDetails(Integer userId) {
        if(userId==null || userId==0){
            throw new BadRequestException("Missing seller Id");
        }
        Optional<SellerEntity> sellerEntityOpt = sellerRepository.findBySellerId(userId);
        SellerEntity sellerEntity = null;
        if(sellerEntityOpt.isEmpty()){
            throw new NotFoundException("Seller not found");
        } else {
            sellerEntity = sellerEntityOpt.get();
        }
        UserDto userDtoResponse = new UserDto();
        createSellerDtoFromSellerEntity(sellerEntity, userDtoResponse);

        return new ResponseEntity<>(userDtoResponse, HttpStatus.OK);
    }

    private void createSellerDtoFromSellerEntity(SellerEntity sellerEntity, UserDto userDto) {
        userDto.setUserId(sellerEntity.getSellerId());
        userDto.setEmail(sellerEntity.getEmail());
        userDto.setPhoneNo(sellerEntity.getPhoneNo());
        userDto.setWhatsappUrl(CommonUtils.createWhatsAppUrl(sellerEntity.getPhoneNo()));
        userDto.setfName(sellerEntity.getfName());
        userDto.setlName(sellerEntity.getlName());
        userDto.setRole(sellerEntity.getRoles().get(0));
    }

    public ResponseEntity<UserDto> updateUserDetails(UserDto userDto) {
        if(userDto!=null && (userDto.getUserId()==0 || userDto.getUserId()==null)){
            throw new BadRequestException("Missing user id");
        }
        Optional<SellerEntity> userEnityOpt = sellerRepository.findBySellerId(userDto.getUserId());
        SellerEntity userEntity = null;
        if(userEnityOpt.isEmpty()){
            throw new NotFoundException("Seller not found");
        } else {
            userEntity = userEnityOpt.get();
        }
        createSellerEntityFromSellerDto(userEntity, userDto);
        //update userEntity
        userEntity = sellerRepository.save(userEntity);

        createSellerDtoFromSellerEntity(userEntity, userDto);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }
}
