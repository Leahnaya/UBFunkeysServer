package com.icedberries.UBFunkeysServer.service.impl;

import com.icedberries.UBFunkeysServer.domain.Cleaning;
import com.icedberries.UBFunkeysServer.repository.CleaningRepository;
import com.icedberries.UBFunkeysServer.service.CleaningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CleaningServiceImpl implements CleaningService {

    private final CleaningRepository cleaningRepository;

    public List<Cleaning> findAll() {
        return cleaningRepository.findAll();
    }

    @Override
    public Cleaning save(Cleaning cleaning) {
        return cleaningRepository.save(cleaning);
    }

    @Override
    public Optional<Cleaning> findByRid(String rid) {
        return cleaningRepository.findByRid(rid);
    }

    @Override
    public Integer getCostById(Integer id) {
        return cleaningRepository.getCostById(id);
    }

    @Override
    public String getRidById(Integer id) {
        return cleaningRepository.getRidById(id);
    }
}
