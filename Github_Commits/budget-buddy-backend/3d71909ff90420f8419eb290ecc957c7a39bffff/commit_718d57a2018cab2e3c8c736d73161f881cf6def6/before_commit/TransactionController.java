package com.robo.project.controllers;


import com.robo.project.mappers.BalanceRestMapper;
import com.robo.project.mappers.dto.TransactionDto;
import com.robo.project.services.BalanceService;
import com.robo.project.util.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api/v1/transactions")
@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final BalanceService balanceService;
    private final BalanceRestMapper mapper;

    @GetMapping("{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        var transaction = balanceService.findById(id);
        return new ResponseEntity<>(
                mapper.toDto(transaction)
                , HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addNewTransaction(@RequestBody TransactionDto transactionDto) {
        var transaction = balanceService.save(mapper.toDomain(transactionDto));
        return new ResponseEntity<>(
                Constant.TRANSACTION_SAVED
                , HttpStatus.OK);
    }
}
