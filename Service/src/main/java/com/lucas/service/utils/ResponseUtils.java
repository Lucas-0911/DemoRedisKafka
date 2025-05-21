package com.lucas.service.utils;

import com.lucas.service.model.dto.ResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;

public class ResponseUtils {

    /**
     * Tạo response thành công với dữ liệu
     *
     * @param data    Dữ liệu trả về
     * @param message Thông báo
     * @param request HttpServletRequest để lấy URL
     * @return ResponseEntity chứa thông tin response
     */
    public static <T> ResponseEntity<ResponseDTO<T>> success(T data, String message, HttpServletRequest request) {
        ResponseDTO<T> response = ResponseDTO.<T>builder()
                .code(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .url(request.getRequestURI())
                .time(new Date())
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Tạo response thành công với dữ liệu và message mặc định
     *
     * @param data    Dữ liệu trả về
     * @param request HttpServletRequest để lấy URL
     * @return ResponseEntity chứa thông tin response
     */
    public static <T> ResponseEntity<ResponseDTO<T>> success(T data, HttpServletRequest request) {
        return success(data, "Thành công", request);
    }

    /**
     * Tạo response lỗi
     *
     * @param status  HttpStatus của lỗi
     * @param message Thông báo lỗi
     * @param request HttpServletRequest để lấy URL
     * @return ResponseEntity chứa thông tin response
     */
    public static <T> ResponseEntity<ResponseDTO<T>> error(HttpStatus status, String message, HttpServletRequest request) {
        ResponseDTO<T> response = ResponseDTO.<T>builder()
                .code(status.value())
                .message(message)
                .url(request.getRequestURI())
                .time(new Date())
                .build();
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Tạo response lỗi Bad Request
     *
     * @param message Thông báo lỗi
     * @param request HttpServletRequest để lấy URL
     * @return ResponseEntity chứa thông tin response
     */
    public static <T> ResponseEntity<ResponseDTO<T>> badRequest(String message, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Tạo response lỗi Not Found
     *
     * @param message Thông báo lỗi
     * @param request HttpServletRequest để lấy URL
     * @return ResponseEntity chứa thông tin response
     */
    public static <T> ResponseEntity<ResponseDTO<T>> notFound(String message, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, message, request);
    }

    /**
     * Tạo response lỗi Internal Server Error
     *
     * @param message Thông báo lỗi
     * @param request HttpServletRequest để lấy URL
     * @return ResponseEntity chứa thông tin response
     */
    public static <T> ResponseEntity<ResponseDTO<T>> serverError(String message, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }
}