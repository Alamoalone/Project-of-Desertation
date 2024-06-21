package com.SwapToSustain.Server.Service;

import com.SwapToSustain.Server.Converter.DTOConverter;
import com.SwapToSustain.Server.DTO.UserProfile;
import com.SwapToSustain.Server.DTO.UserProfileCompact;
import com.SwapToSustain.Server.DTO.UserSearchCriteria;
import com.SwapToSustain.Server.Model.UserAccountInfoModel;
import com.SwapToSustain.Server.Model.UserPostModel;
import com.SwapToSustain.Server.Repository.CustomRepository;
import com.SwapToSustain.Server.Repository.UserInfoRepository;
import com.SwapToSustain.Server.Repository.UserPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class SearchService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private UserPostRepository userPostRepository;

    @Autowired
    private DTOConverter dtoConverter;

    @Autowired
    private CustomRepository customRepository;

    public List<UserProfileCompact> findUsers(UserSearchCriteria userSearchCriteria) {
        List<UserProfileCompact> userProfileCompacts = new ArrayList<>();

        if (!Objects.equals(userSearchCriteria.getUserName(), "null")) {

            UserAccountInfoModel userAccountInfoModel = userInfoRepository.findByUserName(userSearchCriteria.getUserName());
            List<UserAccountInfoModel> userAccountInfoModelList = new ArrayList<>();
            userAccountInfoModelList.add(userAccountInfoModel);

            dtoConverter.convertDTOForCompactProfile(userProfileCompacts, userAccountInfoModelList);

        } else {

            List<UserAccountInfoModel> userAccountInfoModelList = customRepository.findByDynamicCriteria(userSearchCriteria);
            dtoConverter.convertDTOForCompactProfile(userProfileCompacts, userAccountInfoModelList);

        }

        return userProfileCompacts;
    }

    public UserProfile getSingleUser(String userName) {
        UserAccountInfoModel userAccountInfoModelFound = userInfoRepository.findByUserName(userName);
        List<UserPostModel> userPostModels = userPostRepository.findAllByUserID(userAccountInfoModelFound.getUserID());

        UserProfile userProfile = new UserProfile();

        dtoConverter.convertDTO(userPostModels ,userAccountInfoModelFound, userProfile);

        return userProfile;
    }
}
