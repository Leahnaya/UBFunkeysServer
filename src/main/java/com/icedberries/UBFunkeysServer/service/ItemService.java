package com.icedberries.UBFunkeysServer.service;

import com.icedberries.UBFunkeysServer.domain.Item;

import java.util.List;
import java.util.Optional;

public interface ItemService {

    List<Item> findAll();

    Item save(Item item);

    Optional<Item> findByRid(String rid);

    Integer getCostById(Integer id);

    String getRidById(Integer id);
}
