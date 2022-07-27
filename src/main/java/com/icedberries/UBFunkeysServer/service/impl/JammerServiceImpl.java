package com.icedberries.UBFunkeysServer.service.impl;

import com.icedberries.UBFunkeysServer.domain.Jammer;
import com.icedberries.UBFunkeysServer.repository.JammerRepository;
import com.icedberries.UBFunkeysServer.service.JammerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JammerServiceImpl implements JammerService {

    private final JammerRepository jammerRepository;

    @Override
    public Integer getCostById(Integer id) {
        return jammerRepository.getCostById(id);
    }

    @Override
    public Jammer save(Jammer jammer) {
        return jammerRepository.save(jammer);
    }

    @Override
    public Optional<Jammer> findByQty(Integer qty) {
        return jammerRepository.findByQty(qty);
    }

    @Override
    public String getRidById(Integer id) {
        return jammerRepository.getRidById(id);
    }

    @Override
    public List<Jammer> findAll() {
        return jammerRepository.findAll();
    }

    @Override
    public Integer getQtyById(Integer id) {
        return jammerRepository.getQtyById(id);
    }
}
