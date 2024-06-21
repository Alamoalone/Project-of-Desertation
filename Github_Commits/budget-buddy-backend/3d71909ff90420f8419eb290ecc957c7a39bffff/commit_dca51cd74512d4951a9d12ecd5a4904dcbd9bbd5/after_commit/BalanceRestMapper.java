package com.robo.project.mappers;


import com.robo.project.model.Transaction;
import org.javamoney.moneta.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", imports = {Money.class})
public interface BalanceRestMapper {

    @Mapping(target = "value" ,expression = "java(Money.of(transactionDto.getMoneyValue(), \"USD\"))")
    Transaction toDomain(TransactionDto transactionDto);


    @Mapping(target = "moneyValue", expression = "java(transaction.getValue().getNumber().doubleValueExact())")
    TransactionDto toDto(Transaction transaction);

}
