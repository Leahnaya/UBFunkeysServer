package com.icedberries.UBFunkeysServer.ArkOne.Plugins;

import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;

@Service
public class TrunkPlugin {

    private final Integer LOOT_BALANCE = 2500;

    public String GetUserAssets() {
        //TODO: IMPLEMENT ME TO READ FROM USER PROFILE

        return "<h10_0><gua><m id=\"80041a\" /></gua></h10_0>";
    }

    public String GetLootBalance() {
        return "<h10_0><glb b=\"" + LOOT_BALANCE + "\" /></h10_0>";
    }

    public String GetFamiliarsList() {
        // Create list of all familiar IDs
        List<String> familiarIds = Arrays.asList("80036a", "80035a", "80034a", "80033a", "80032a", "80031a", "80030a",
                "80029a", "80028a", "80027a", "80026a", "80025a", "80017a", "80016a", "80015a", "80007a", "80006a",
                "80005a", "80004a", "80003a", "80002a", "80001a", "80000a");

        // Build the list
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<h10_0><gfl>");

        // Iterate over the IDs to append them
        for (String id : familiarIds) {
            stringBuilder.append("<f rid=\"" + id + "\" id=\"" + id + "\" c=\"100\" dc=\"50\" h=\"720\" d=\"false\" />");
        }

        // Add closing tags
        stringBuilder.append("</gfl></h10_0>");

        // Return the list
        return stringBuilder.toString();
    }

    public String BuyFamiliar(Element element) {
        // We always return LOOT_BALANCE so players are never charged for these items
        return "<h10_0><bf id=\"" + element.getAttribute("id") + "\" b=\"" + LOOT_BALANCE + "\" /></h10_0>";
    }
}
