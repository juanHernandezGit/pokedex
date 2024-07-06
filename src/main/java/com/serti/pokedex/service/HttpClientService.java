package com.serti.pokedex.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.fluent.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HttpClientService {

    @Autowired
    private ObjectMapper objectMapper;

    public <T> T getForObject(String url, Class<T> responseType) throws IOException {
        String response = Request.get(url)
                .execute()
                .returnContent()
                .asString();
        return objectMapper.readValue(response, responseType);
    }
}
