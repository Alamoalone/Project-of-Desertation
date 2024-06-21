package com.bc.utilities;

import lombok.extern.slf4j.Slf4j;

/**
 * Class implementing methods as defined by ISO/IEC 9797-1 Padding.
 */
@Slf4j
public class ISOIEC97971Padding {

    /**
     * Pad input data as per ISO/IEC 9797-1 Method 1 padding.
     * Notes:
     * - ISO/IEC 9797-1 Method 1 padding requires data to be padded to a pre-defined, n byte block by padding the
     * data with '0' bits to make the padded data is a multiple of n.
     *  - If the supplied data is already a multiple of n, no padding is performed.
     *  - Below method pads data to make it a multiple of 8 byte blocks.
     *
     * @param inputData Input data to be padded.
     * @return Padded input data as per ISO/IEC 9797-1 Method 1 padding.
     */
    public static String performIsoIec97971Method1Padding(String inputData){
        // Constants
        final int BLOCK_SIZE = 16; // Uses 16 here, since the input data is in hexadecimal format.
        final String PADDING_CHAR = "0"; // Padding character 0.
        // Variables
        int inputDataLength = inputData.length();
        int requiredInputDataLength = ((int)
                Math.ceil((float)
                        inputDataLength /
                        BLOCK_SIZE)
                * BLOCK_SIZE
        );
        log.debug("ISO 97971 Padding Method 1, input data: {}.", inputData);
        // Check if transaction data is multiple of 16, else pad with x"0" chars till the length is a multiple of 16.
        if (inputDataLength  == requiredInputDataLength){
            return inputData;
        } else {
            return Padding.padString(inputData,
                    PADDING_CHAR,
                    requiredInputDataLength,
                    false
            );
        }
    }
    /**
     * Pad input data as per ISO/IEC 9797-1 Method 2 padding.
     * Notes:
     * - ISO/IEC 9797-1 Method 2 padding requires data to be padded to ensure it is an n byte block, however,
     * the data must first be padded with a mandatory bit value 1, if this data is not a multiple on n then no further
     * padding is necessary.
     *  - If the data after padding with the mandatory bit value 1 is not a multiple of n, the data is padded with
     *  required number of 0 bits to make  it a multiple of n.
     *  - Below method pads data to make it a multiple of 8 byte blocks.
     *
     * @param inputData Input data to be padded.
     * @return Padded input data as per ISO/IEC 9797-1 Method 2 padding.
     */
    public static String performIsoIec97971Method2Padding(String inputData){
        // Constants
        final int BLOCK_SIZE = 16; // Uses 16 here, since the input data is in hexadecimal format.
        final String MANDATORY_PADDING_CHAR = "80"; // Mandatory bit 1 padding character, i.e., 1000 0000.
        final String OPTIONAL_PADDING_CHAR = "0"; // Optional bit 0 padding character, i.e., 0000 0000.
        // Variables
        inputData = inputData + MANDATORY_PADDING_CHAR;
        int inputDataLength = inputData.length();
        int requiredInputDataLength = ((int)
                Math.ceil((float)
                        inputDataLength /
                        BLOCK_SIZE)
                * BLOCK_SIZE
        );
        log.debug("ISO 97971 Padding Method 2, input data: {}.", inputData);
        // Check if transaction data is multiple of 16, else pad with x"0" chars till the length is a multiple of 16.
        if (inputDataLength  == requiredInputDataLength){
            return inputData;
        } else {
            return Padding.padString(inputData,
                    OPTIONAL_PADDING_CHAR,
                    requiredInputDataLength,
                    false);
        }
    }

}