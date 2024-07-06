package com.serti.pokedex.exception;

public class ApiRequestException extends RuntimeException {
    public ApiRequestException(String url, String message) {
        super("Error en la solicitud a la API: " + url + ". Mensaje: " + message);
    }
}