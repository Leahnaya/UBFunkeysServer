package com.icedberries.UBFunkeysServer.service.impl;

import com.icedberries.UBFunkeysServer.domain.Crib;
import com.icedberries.UBFunkeysServer.repository.CribRepository;
import com.icedberries.UBFunkeysServer.service.CribService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CribServiceImpl implements CribService {

    private final CribRepository cribRepository;

    @Override
    public Integer count() {
        return Math.toIntExact(cribRepository.count());
    }

    @Override
    public void save(Crib crib) {
        cribRepository.save(crib);
    }

    @Override
    public Boolean existsByCribName(String cribName) {
        return cribRepository.existsByCribName(cribName);
    }

    @Override
    public Crib findByCribName(String cribName) {
        return cribRepository.findByCribName(cribName);
    }

    @Override
    public Crib findById(Integer id) {
        return cribRepository.findById(id).orElse(null);
    }
}
