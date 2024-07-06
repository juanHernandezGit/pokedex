package com.serti.pokedex.audit;

import org.springframework.stereotype.Service;

@Service
public class LogService {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void saveLog(LogEntity logEntity) {
        logRepository.save(logEntity);
    }
}
