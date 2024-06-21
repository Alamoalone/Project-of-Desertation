package com.errand.mapper;

import com.errand.dto.RatingDto;
import com.errand.models.Rating;

import static com.errand.mapper.ClientMapper.mapToClient;
import static com.errand.mapper.ClientMapper.mapToClientDto;
import static com.errand.mapper.ServiceProviderMapper.toServiceProvider;
import static com.errand.mapper.ServiceProviderMapper.toServiceProviderDto;
import static com.errand.mapper.TaskMapper.mapToTask;
import static com.errand.mapper.TaskMapper.mapToTaskDto;

public class RatingMapper {

    public static Rating maptoRatingFromClient(RatingDto ratingDto){

        Rating rating = Rating.builder()
                .serviceProviderRating(ratingDto.getServiceProviderRating())
                .serviceProviderRatingDescription(ratingDto.getServiceProviderRatingDescription())
                .task(mapToTask(ratingDto.getTaskDto()))
                .client(mapToClient(ratingDto.getClientDto()))
                .serviceProvider(toServiceProvider(ratingDto.getServiceProviderDto()))
                .build();
        return rating;
    }


    public static RatingDto mapToRatingDtoFromClient(Rating rating){
        RatingDto.RatingDtoBuilder builder = RatingDto.builder()
                .serviceProviderRating(rating.getServiceProviderRating())
                .serviceProviderRatingDescription(rating.getServiceProviderRatingDescription())
                .taskDto(mapToTaskDto(rating.getTask()));

        if (rating.getClient() != null){
            builder.clientDto(mapToClientDto(rating.getClient()));
        }
        return builder.build();
    }

    public static Rating mapToRatingFromServiceProvider(RatingDto ratingDto){

        Rating rating = Rating.builder()
                .clientRatingDescription(ratingDto.getClientRatingDescription())
                .clientRating(ratingDto.getClientRating())
                .task(mapToTask(ratingDto.getTaskDto()))
                .serviceProvider(toServiceProvider(ratingDto.getServiceProviderDto()))
                .client(mapToClient(ratingDto.getClientDto()))
                .build();
        return rating;
    }

    public static RatingDto maptoRatingDtoFromServiceProvider(Rating rating){
        RatingDto.RatingDtoBuilder builder = RatingDto.builder()
                .clientRating(rating.getClientRating())
                .clientRatingDescription(rating.getClientRatingDescription())
                .taskDto(mapToTaskDto(rating.getTask()));

        if(rating.getServiceProvider() != null){
            builder.serviceProviderDto(toServiceProviderDto(rating.getServiceProvider()));
        }

        return  builder.build();
    }


}
