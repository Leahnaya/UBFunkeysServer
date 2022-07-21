package com.icedberries.UBFunkeysServer.ArkOne.Plugins;

import org.springframework.stereotype.Service;

@Service
public class TrunkPlugin {

    private final Integer LOOT_BALANCE = 2500;

    public String GetUserAssets() {
        //TODO: IMPLEMENT ME
        return "<h10_0><gua></gua></h10_0>";
    }

    public String GetLootBalance() {

        return "<h10_0><glb b=\"" + LOOT_BALANCE + "\" /></h10_0>";
    }
}
