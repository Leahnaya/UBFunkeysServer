package com.icedberries.UBFunkeysServer.DatabaseSetup;

import com.icedberries.UBFunkeysServer.domain.Familiar;
import com.icedberries.UBFunkeysServer.domain.Jammer;
import com.icedberries.UBFunkeysServer.service.FamiliarService;
import com.icedberries.UBFunkeysServer.service.JammerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class TrunkData {

    @Autowired
    FamiliarService familiarService;

    @Autowired
    JammerService jammerService;

    // List of all familiar ids
    private final List<String> familiarIds = Arrays.asList("80036a", "80035a", "80034a", "80033a", "80032a", "80031a", "80030a",
            "80029a", "80028a", "80027a", "80026a", "80025a", "80017a", "80016a", "80015a", "80007a", "80006a",
            "80005a", "80004a", "80003a", "80002a", "80001a", "80000a");

    public static final String JAMMER_RID = "80014a";
    private final List<Integer> JAMMER_PACKAGE_QTYS = Arrays.asList(1, 5, 10, 25, 50, 100);
    String PackageOf1 = "<j id=\"1\" rid=\"80014a\" c=\"10\" q=\"1\" d=\"\" />";
    String PackageOf5 = "<j id=\"2\" rid=\"80014a\" c=\"50\" q=\"5\" d=\"\" />";
    String PackageOf10 = "<j id=\"3\" rid=\"80014a\" c=\"100\" q=\"10\" d=\"\" />";
    String PackageOf25 = "<j id=\"4\" rid=\"80014a\" c=\"250\" q=\"25\" d=\"\" />";
    String PackageOf50 = "<j id=\"5\" rid=\"80014a\" c=\"500\" q=\"50\" d=\"\" />";
    String PackageOf100 = "<j id=\"6\" rid=\"80014a\" c=\"1000\" q=\"100\" d=\"\" />";

    private final Integer JAMMER_COST = 10;

    private final Integer FAMILIAR_COST = 100;
    private final Integer FAMILIAR_DISCOUNT_COST = 50;
    private final Integer FAMILIAR_DURATION = 720;

    @EventListener(ApplicationReadyEvent.class)
    public void insertFamiliars() {
        // Iterate over the familiar ids
        int idNum = 0;
        for (String id : familiarIds) {
            // Attempt to get it from the DB
            Familiar familiar = familiarService.findByRid(id).orElse(null);

            // Only insert if the data is null
            if (familiar == null) {
                // Build a new familiar to insert
                Familiar newFamiliar = Familiar.builder()
                        .id(idNum)
                        .rid(id)
                        .cost(FAMILIAR_COST)
                        .discountedCost(FAMILIAR_DISCOUNT_COST)
                        .duration(FAMILIAR_DURATION)
                        .build();

                // Save it to the db
                familiarService.save(newFamiliar);
            }

            // Increment id
            idNum++;
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void insertJammers() {
        // Iterate over the jammer package quantities
        int idNum = 0;
        for (Integer qty : JAMMER_PACKAGE_QTYS) {
            // Attempt to get from the DB
            Jammer jammer = jammerService.findByQty(qty).orElse(null);

            // Only insert if the data is null
            if (jammer == null) {
                // Build a new jammer to insert
                Jammer newJammer = Jammer.builder()
                        .id(idNum)
                        .rid(JAMMER_RID)
                        .cost(JAMMER_COST * qty)
                        .qty(qty)
                        .build();

                // Save it to the db
                jammerService.save(newJammer);
            }

            // Increment id
            idNum++;
        }
    }
}
