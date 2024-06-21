package com.robo.project.mappers;


import com.robo.project.mappers.dto.TransactionDto;
import com.robo.project.model.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = MoneyMapper.class)
public interface BalanceRestMapper {

    Transaction toDomain(TransactionDto transactionDto);

    TransactionDto toDto(Transaction transaction);

}
