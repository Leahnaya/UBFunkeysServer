package com.icedberries.UBFunkeysServer.service.impl;

import com.icedberries.UBFunkeysServer.domain.Item;
import com.icedberries.UBFunkeysServer.repository.ItemRepository;
import com.icedberries.UBFunkeysServer.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    @Override
    public Item save(Item item) {
        return itemRepository.save(item);
    }

    @Override
    public Optional<Item> findByRid(String rid) {
        return itemRepository.findByRid(rid);
    }

    @Override
    public Integer getCostById(Integer id) {
        return itemRepository.getCostById(id);
    }

    @Override
    public String getRidById(Integer id) {
        return itemRepository.getRidById(id);
    }
}
