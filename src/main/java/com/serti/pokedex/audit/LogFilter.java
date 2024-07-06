package com.serti.pokedex.audit;


import java.io.IOException;
import java.time.LocalDateTime;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class LogFilter implements Filter {

    private final LogRepository logRepository;

    public LogFilter(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "anonymous";

        String url = httpRequest.getRequestURI();
        String action = httpRequest.getMethod();
        LocalDateTime timestamp = LocalDateTime.now();

        chain.doFilter(request, response);

        String responseBody = httpResponse.getStatus() == HttpServletResponse.SC_OK ? "Success" : "Failure";
        LogEntity logEntity = new LogEntity();
        logEntity.setUsername(username);
        logEntity.setAction(action);
        logEntity.setUrl(url);
        logEntity.setResponse(responseBody);
        logEntity.setTimestamp(timestamp);

        logRepository.save(logEntity);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

}

