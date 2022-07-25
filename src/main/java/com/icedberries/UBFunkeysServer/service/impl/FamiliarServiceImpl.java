package com.icedberries.UBFunkeysServer.service.impl;

import com.icedberries.UBFunkeysServer.domain.Familiar;
import com.icedberries.UBFunkeysServer.repository.FamiliarRepository;
import com.icedberries.UBFunkeysServer.service.FamiliarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FamiliarServiceImpl implements FamiliarService {

    private final FamiliarRepository familiarRepository;

    @Override
    public Optional<Familiar> findById(String id) {
        return familiarRepository.findById(id);
    }

    @Override
    public Familiar save(Familiar familiar) {
        return familiarRepository.save(familiar);
    }

    @Override
    public List<Familiar> findAll() {
        return familiarRepository.findAll();
    }

    @Override
    public Integer getCostById(String id) {
        return familiarRepository.getCostById(id);
    }
}
