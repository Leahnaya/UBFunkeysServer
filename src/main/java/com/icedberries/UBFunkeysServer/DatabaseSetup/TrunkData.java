package com.icedberries.UBFunkeysServer.DatabaseSetup;

import com.icedberries.UBFunkeysServer.domain.Familiar;
import com.icedberries.UBFunkeysServer.service.FamiliarService;
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

    // List of all familiar ids
    private final List<String> familiarIds = Arrays.asList("80036a", "80035a", "80034a", "80033a", "80032a", "80031a", "80030a",
            "80029a", "80028a", "80027a", "80026a", "80025a", "80017a", "80016a", "80015a", "80007a", "80006a",
            "80005a", "80004a", "80003a", "80002a", "80001a", "80000a");
    private final Integer FAMILIAR_COST = 100;
    private final Integer FAMILIAR_DISCOUNT_COST = 50;
    private final Integer FAMILIAR_DURATION = 720;

    @EventListener(ApplicationReadyEvent.class)
    public void insertFamiliars() {
        // Iterate over the familiar ids
        for (String id : familiarIds) {
            // Attempt to get it from the DB
            Familiar familiar = familiarService.findById(id).orElse(null);

            // Only insert if the data is null
            if (familiar == null) {
                // Build a new familiar to insert
                Familiar newFamiliar = Familiar.builder()
                        .id(id)
                        .cost(FAMILIAR_COST)
                        .discountedCost(FAMILIAR_DISCOUNT_COST)
                        .duration(FAMILIAR_DURATION)
                        .build();

                // Save it to the db
                familiarService.save(newFamiliar);
            }
        }
    }
}
