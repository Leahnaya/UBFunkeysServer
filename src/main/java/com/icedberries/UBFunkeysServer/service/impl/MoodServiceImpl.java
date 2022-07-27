package com.icedberries.UBFunkeysServer.service.impl;

import com.icedberries.UBFunkeysServer.domain.Mood;
import com.icedberries.UBFunkeysServer.repository.MoodRepository;
import com.icedberries.UBFunkeysServer.service.MoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MoodServiceImpl implements MoodService {

    private final MoodRepository moodRepository;

    public List<Mood> findAll() {
        return moodRepository.findAll();
    }

    @Override
    public Mood save(Mood mood) {
        return moodRepository.save(mood);
    }

    @Override
    public Optional<Mood> findByRid(String rid) {
        return moodRepository.findByRid(rid);
    }

    @Override
    public Integer getCostById(Integer id) {
        return moodRepository.getCostById(id);
    }

    @Override
    public String getRidById(Integer id) {
        return moodRepository.getRidById(id);
    }
}
