package com.bc.application.service.impl;

import com.bc.application.domain.CryptogramRequest;
import com.bc.application.domain.CryptogramResponse;
import com.bc.application.enumeration.CryptogramVersionNumber;
import com.bc.application.enumeration.EMVUDKDerivationMethod;
import com.bc.application.enumeration.PaymentScheme;
import com.bc.application.port.in.rest.cryptogramfunctions.command.GenerateApplicationCryptogramCommand;
import com.bc.application.port.in.rest.cryptogramfunctions.mapper.GenerateACCommandToDomainMapper;
import com.bc.application.service.CryptogramFunctionsService;
import com.bc.utilities.*;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
/**
 * Core domain service hosting the methods for performing Visa Payment scheme specific cryptogram related functions.
 */
@Slf4j
@ApplicationScoped
public class VisaCryptogramServiceImpl
        extends CryptogramFunctionsService<GenerateApplicationCryptogramCommand,
        GenerateACCommandToDomainMapper>
        implements LoggerUtility {
    /**
     * Driver method for generating an Application Cryptogram.
     * @param command Command object with the Application Cryptogram generation request.
     * @return CryptogramResponse domain object with the generated cryptogram value.
     */
    @Override
    public CryptogramResponse getApplicationCryptogram(GenerateApplicationCryptogramCommand command) {
        final String CVN_NAME = "CVN";
        final String CVR_NAME = "CVR";
        // Log command object
        logDebug(log, "Command object received: {}.", command);
        // Map command to domain
        CryptogramRequest cryptogramRequest = buildDomainObjectFromCommand(command);
        // Parsed Visa IAD
        Map<String, String> visaIad =  getParsedVisaIad(cryptogramRequest.getIssuerApplicationData());
        // Set Cryptogram Version Number
        log.debug("CVN from Parsed IAD: {}.", visaIad.get(CVN_NAME));
        CryptogramVersionNumber cryptogramVersionNumber = CryptogramVersionNumber.valueOf(
                visaIad.get(CVN_NAME)
        );
        String cardVerificationResults = visaIad.get(CVR_NAME);
        // Generate Unique Derivation Key
        String uniqueDerivationKey = getUniqueDerivationKey(cryptogramRequest.getIssuerMasterKey(),
                cryptogramRequest.getPan(),
                cryptogramRequest.getPanSequenceNumber(),
                cryptogramVersionNumber
        );
        // Generate Session Key
        String sessionKey = getSessionKey(uniqueDerivationKey,
                cryptogramRequest.getApplicationTransactionCounter(),
                cryptogramVersionNumber
        );
        // Generate Visa cryptogram, and build and return REST API response object
        return buildResponseObjectFromDomain(generateCryptogram(cryptogramRequest,
                sessionKey,
                cryptogramVersionNumber,
                cardVerificationResults)
        );
    }
    /**
     * Method to build and map the command object to Cryptogram Request domain object.
     * @param command Application Cryptogram Request command object.
     * @return CryptogramRequest domain object generated by mapping command object.
     */
    @Override
    protected CryptogramRequest buildDomainObjectFromCommand(GenerateApplicationCryptogramCommand command) {
        return commandToDomainMapper.mapGenerateACCommandToDomain(command);
    }

    /**
     * Call Visa IAD parser with IAD from request as input.
     * @param issuerApplicationData IAD from request.
     * @return Parsed Visa IAD map.
     */
    private Map<String, String> getParsedVisaIad(String issuerApplicationData){
        return new VisaIADParser(
                issuerApplicationData)
                .parseIad();
    }
    /**
     * Method to derive Unique Derivation Key (UDK) from Issuer Master Key (IMK) for cryptogram generation.
     * @param issuerMasterKey Issuer Master Key from request.
     * @param pan Primary Account Number from request.
     * @param panSequenceNumber PAN sequence number from request.
     * @param cryptogramVersionNumber Cryptogram version number determined from Issuer Application Data.
     * @return UDK generated from IMK.
     */
    @Override
    protected String getUniqueDerivationKey(String issuerMasterKey,
                                            String pan,
                                            String panSequenceNumber,
                                            CryptogramVersionNumber cryptogramVersionNumber) {
        EMVUniqueDerivationKeyDerivator emvUdkDerivator = new EMVUniqueDerivationKeyDerivator(issuerMasterKey,
                pan,
                panSequenceNumber,
                PaymentScheme.VISA,
                cryptogramVersionNumber,
                EMVUDKDerivationMethod.METHOD_A
        );
        // The UDK derivation must be enhanced for CVN 22, as CVN 22 uses METHOD_B.
        return emvUdkDerivator.generateUniqueDerivationKey();
    }
    /**
     * Method to derive Unique Derivation Key (UDK) from Issuer Master Key (IMK) for cryptogram generation.
     * @param uniqueDerivationKey UDK deriver from IMK.
     * @param applicationTransactionCounter Application Transaction Counter from request.
     * @param cryptogramVersionNumber Cryptogram version number determined from Issuer Application Data.
     * @return Session Key generated from UDK.
     */
    @Override
    protected String getSessionKey(String uniqueDerivationKey,
                                   String applicationTransactionCounter,
                                   CryptogramVersionNumber cryptogramVersionNumber) {
        EMVSessionKeyDerivator emvSessionKeyDerivator = new EMVSessionKeyDerivator(uniqueDerivationKey,
                applicationTransactionCounter,
                cryptogramVersionNumber
        );
        return emvSessionKeyDerivator.generateSessionKey();
    }
    /**
     * Method to build and map the Cryptogram Response domain object to Response DTO class.
     * @param arqc Application Cryptogram generated.
     * @return CryptogramResponse object containing generated Application Cryptogram.
     */
    @Override
    protected CryptogramResponse buildResponseObjectFromDomain(String arqc) {
        CryptogramResponse cryptogramResponse = new CryptogramResponse();
        cryptogramResponse.setRequestCryptogram(arqc);
        return cryptogramResponse;
    }
    /**
     * Method to call the Payment Scheme specific cryptogram generation request.
     * @param cryptogramRequest Cryptogram Request domain object.
     * @return Application Cryptogram generated by Payment Scheme specific service.
     */
    @Override
    protected String generateCryptogram(CryptogramRequest cryptogramRequest,
                                        String sessionKey,
                                        CryptogramVersionNumber cryptogramVersionNumber,
                                        String cardVerificationResults) {
        VisaApplicationCryptogramGenerator visaApplicationCryptogramGenerator = new VisaApplicationCryptogramGenerator();
        return visaApplicationCryptogramGenerator.generateVisaApplicationCryptogram(cryptogramRequest,
                sessionKey,
                cryptogramVersionNumber,
                cardVerificationResults
        );
    }
}