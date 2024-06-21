package com.bc.application.port.in.rest.cryptogramfunctions.command;

import com.bc.utilities.LoggerUtility;
import com.bc.utilities.SelfValidator;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import static com.bc.model.pattern.CommonPattern.*;
/**
 * Application Cryptogram Generation command class with selfvalidator.
 */
@Slf4j
public class GenerateApplicationCryptogramCommand
        extends SelfValidator<GenerateApplicationCryptogramCommand>
        implements LoggerUtility {
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

    /**
     * All args constructor for creating GenerateApplicationCryptogramCommand
     * @param pan PAN.
     * @param panSequenceNumber PAN Sequence number.
     * @param issuerMasterKey Issuer Master Key.
     * @param amountAuthorised Amount authorised.
     * @param amountOther Amount other.
     * @param terminalCountryCode Terminal Country Code.
     * @param terminalVerificationResults Terminal Verification Results.
     * @param transactionCurrencyCode Transaction Currency Code.
     * @param transactionDate Transaction Date.
     * @param transactionType Transaction Type.
     * @param unpredictableNumber Unpredictable Number.
     * @param applicationInterchangeProfile Application Interchange Profile.
     * @param applicationTransactionCounter Application Transaction Counter.
     * @param issuerApplicationData Issuer Application Data.
     */
    public GenerateApplicationCryptogramCommand(String pan,
                                                String panSequenceNumber,
                                                String issuerMasterKey,
                                                String amountAuthorised,
                                                String amountOther,
                                                String terminalCountryCode,
                                                String terminalVerificationResults,
                                                String transactionCurrencyCode,
                                                String transactionDate,
                                                String transactionType,
                                                String unpredictableNumber,
                                                String applicationInterchangeProfile,
                                                String applicationTransactionCounter,
                                                String issuerApplicationData) {
        this.pan = pan;
        this.panSequenceNumber = panSequenceNumber;
        this.issuerMasterKey = issuerMasterKey;
        this.amountAuthorised = amountAuthorised;
        this.amountOther = amountOther;
        this.terminalCountryCode = terminalCountryCode;
        this.terminalVerificationResults = terminalVerificationResults;
        this.transactionCurrencyCode = transactionCurrencyCode;
        this.transactionDate = transactionDate;
        this.transactionType = transactionType;
        this.unpredictableNumber = unpredictableNumber;
        this.applicationInterchangeProfile = applicationInterchangeProfile;
        this.applicationTransactionCounter = applicationTransactionCounter;
        this.issuerApplicationData = issuerApplicationData;
        logInfo(log, "Self validation completed!");
    }

    /**
     * Override method to return a string representation of the class.
     * @return Attributes converted to string.
     */
    @Override
    public String toString() {
        return "GenerateApplicationCryptogramCommand{" +
                "pan='" + pan + '\'' +
                ", panSequenceNumber='" + panSequenceNumber + '\'' +
                ", issuerMasterKey='" + issuerMasterKey + '\'' +
                ", amountAuthorised='" + amountAuthorised + '\'' +
                ", amountOther='" + amountOther + '\'' +
                ", terminalCountryCode='" + terminalCountryCode + '\'' +
                ", terminalVerificationResults='" + terminalVerificationResults + '\'' +
                ", transactionCurrencyCode='" + transactionCurrencyCode + '\'' +
                ", transactionDate='" + transactionDate + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", unpredictableNumber='" + unpredictableNumber + '\'' +
                ", applicationInterchangeProfile='" + applicationInterchangeProfile + '\'' +
                ", applicationTransactionCounter='" + applicationTransactionCounter + '\'' +
                ", issuerApplicationData='" + issuerApplicationData + '\'' +
                '}';
    }

}
