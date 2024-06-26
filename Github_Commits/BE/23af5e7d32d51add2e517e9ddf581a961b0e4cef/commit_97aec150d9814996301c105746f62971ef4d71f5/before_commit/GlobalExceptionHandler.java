package com.devrun.exception;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
	        HttpHeaders headers, HttpStatus status, WebRequest request) {
        // 첫 번째 에러의 defaultMessage를 가져옵니다.
        String errorMessage = exception.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        // 새로운 오류 응답 객체를 만듭니다.
//        ErrorResponse errorResponse = new ErrorResponse();
//        errorResponse.setMessage(errorMessage);

        // ErrorResponse 객체를 포함하는 ResponseEntity를 반환합니다.
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RestApiException.class)
    public ResponseEntity<Object> handleQuizException(final RestApiException e) {
        final ErrorCode errorCode = e.getErrorCode();
        return handleExceptionInternal(errorCode);
    }

    private ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(makeErrorResponse(errorCode));
    }

    private ErrorResponse makeErrorResponse(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .build();
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Object> nullex(final NullPointerException e) {
        Map<String, String> responeMap = new HashMap<>();
        responeMap.put("message", "Can't Find Data, Check Your Request. -By DevRun");
        return ResponseEntity.badRequest().body(responeMap);
    }

    @ExceptionHandler(value = ConstraintViolationException.class) // 유효성 검사 실패 시 발생하는 예외를 처리
    protected ResponseEntity handleException(ConstraintViolationException exception) {
        System.out.println();
        return ResponseEntity.badRequest().body("Failed validation\n" + getResultMessage(exception.getConstraintViolations().iterator()));
    }

//    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
            Map<String ,String> responemap = new HashMap<>();
            responemap.put("message" , "Can't Read HTTP Request, Check Your Request data type. ex json, xml, text, Number, Array, Object etc. -By DevRun");

        return handleExceptionInternal(ex, responemap, headers, status, request);
    }

    protected String getResultMessage(final Iterator<ConstraintViolation<?>> violationIterator) {
        final StringBuilder resultMessageBuilder = new StringBuilder();
        while (violationIterator.hasNext() == true) {
            final ConstraintViolation<?> constraintViolation = violationIterator.next();
            resultMessageBuilder
                    .append("['")
                    .append(getPopertyName(constraintViolation.getPropertyPath().toString())) // 유효성 검사가 실패한 속성
                    .append("' is '")
                    .append(constraintViolation.getInvalidValue()) // 유효하지 않은 값
                    .append("'. ")
                    .append(constraintViolation.getMessage()) // 유효성 검사 실패 시 메시지
                    .append("]");

            if (violationIterator.hasNext() == true) {
                resultMessageBuilder.append(", ");
            }
        }

        return resultMessageBuilder.toString();
    }

    protected String getPopertyName(String propertyPath) {
        return propertyPath.substring(propertyPath.lastIndexOf('.') + 1); // 전체 속성 경로에서 속성 이름만 가져온다.
    }

    @ExceptionHandler(value = StringIndexOutOfBoundsException.class)
    protected ResponseEntity stringIndexOutOfBoundsException(final StringIndexOutOfBoundsException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    protected ResponseEntity InvalidDataAccessResourceUsageException(final InvalidDataAccessResourceUsageException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }


}