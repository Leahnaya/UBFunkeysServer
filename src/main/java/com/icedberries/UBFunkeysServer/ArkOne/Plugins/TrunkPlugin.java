package com.icedberries.UBFunkeysServer.ArkOne.Plugins;

import org.springframework.stereotype.Service;

@Service
public class TrunkPlugin {

    private final Integer LOOT_BALANCE = 2500;

    public String GetUserAssets() {
        //TODO: IMPLEMENT ME
        return "<gua><items /><familiars /><moods /><jammers /></gua>";
    }

    public String GetLootBalance() {

        return "<h10_0><glb b=\"" + LOOT_BALANCE + "\" /></h10_0>";
    }
}
