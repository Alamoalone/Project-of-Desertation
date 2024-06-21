package com.bc.application.port.in.rest.cryptogramfunctions.command;

import com.bc.utilities.SelfValidator;
import jakarta.validation.constraints.Pattern;
import static com.bc.model.pattern.CommonPattern.*;
/**
 * Application Cryptogram Generation command class with selfvalidator.
 */
public class GenerateApplicationCryptogramCommand extends SelfValidator<GenerateApplicationCryptogramCommand> {
    @Pattern(regexp = IS_A_16_DIGIT_HEXADECIMAL_NUMBER)
    public String pan;
    @Pattern(regexp = IS_A_1_OR_2_DIGIT_DECIMAL_NUMBER)
    public String panSequenceNumber;
    @Pattern(regexp = IS_A_VALID_TDEA_KEY)
    public String issuerMasterKey;
    @Pattern(regexp = IS_A_1_TO_12_DIGIT_DECIMAL_NUMBER)
    public String amountAuthorised;
    @Pattern(regexp = IS_A_1_TO_12_DIGIT_DECIMAL_NUMBER)
    public String amountOther;
    @Pattern(regexp = IS_A_3_DIGIT_DECIMAL_NUMBER)
    public String terminalCountryCode;
    @Pattern(regexp = IS_A_10_DIGIT_HEXADECIMAL_NUMBER)
    public String terminalVerificationResults;
    @Pattern(regexp = IS_A_3_DIGIT_DECIMAL_NUMBER)
    public String transactionCurrencyCode;
    @Pattern(regexp = IS_VALID_ISO_DATE_YYYY_MM_DD)
    public String transactionDate;
    @Pattern(regexp = IS_A_2_DIGIT_HEXADECIMAL_NUMBER)
    public String transactionType;
    @Pattern(regexp = IS_A_8_DIGIT_HEXADECIMAL_NUMBER)
    public String unpredictableNumber;
    @Pattern(regexp = IS_A_4_DIGIT_HEXADECIMAL_NUMBER)
    public String applicationInterchangeProfile;
    @Pattern(regexp = IS_A_1_TO_4_DIGIT_HEXADECIMAL_NUMBER)
    public String applicationTransactionCounter;
    @Pattern(regexp = IS_VALID_IAD_FORMAT)
    public String issuerApplicationData;

}
