package com.bc.exception;

/**
 * Custom runtime exception class which extends IllegalStateException subclass of the Exception class.
 */
public class ClassValidationException extends IllegalStateException {
    public String errorCode;
    /**
     * No argument constructor
     */
    public ClassValidationException(){
        super();
    }
    /**
     * Error message constructor
     * @param message Error message to be included in the exception
     */
    public ClassValidationException(String message){
        super(message);
    }

    /**
     * Error message and cause constructor
     * @param message Error message to be included in the exception
     * @param cause A "Throwable" type cause of the error
     */
    public ClassValidationException(String message, Throwable cause){
        super(message, cause);
    }

    /**
     * Error message, cause and custom error code constructor
     * @param message Error message to be included in the exception
     * @param cause A "Throwable" type cause of the error
     * @param errorCode Custom error code associated with the exception
     */
    public ClassValidationException(String message, Throwable cause, String errorCode){
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Getter method to be used to retrieve custom error code
     * @return Custom error code associated with the exception
     */
    public String getErrorCode(){
        return this.errorCode;
    }

}