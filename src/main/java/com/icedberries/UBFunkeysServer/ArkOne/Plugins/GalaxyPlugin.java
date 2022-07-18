package com.icedberries.UBFunkeysServer.ArkOne.Plugins;

import org.springframework.stereotype.Service;

@Service
public class GalaxyPlugin {

    public String LoadProfileVersion() {
        //TODO: IMPLEMENT THIS WITH PROFILE SAVING
        return "<h7_0><lpv /></h7_0>";
    }

    public String VersionStatisticsRequest() {
        return "<h7_0><vsu id=\"0\" /></h7_0>";
    }
}
