package com.icedberries.UBFunkeysServer.DatabaseSetup;

import com.icedberries.UBFunkeysServer.domain.Cleaning;
import com.icedberries.UBFunkeysServer.domain.Familiar;
import com.icedberries.UBFunkeysServer.domain.Jammer;
import com.icedberries.UBFunkeysServer.domain.Mood;
import com.icedberries.UBFunkeysServer.service.CleaningService;
import com.icedberries.UBFunkeysServer.service.FamiliarService;
import com.icedberries.UBFunkeysServer.service.JammerService;
import com.icedberries.UBFunkeysServer.service.MoodService;
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

    @Autowired
    MoodService moodService;

    @Autowired
    CleaningService cleaningService;

    // Familiars
    private final List<String> familiarIds = Arrays.asList("80036a", "80035a", "80034a", "80033a", "80032a", "80031a", "80030a",
            "80029a", "80028a", "80027a", "80026a", "80025a", "80017a", "80016a", "80015a", "80007a", "80006a",
            "80005a", "80004a", "80003a", "80002a", "80001a", "80000a");
    public static final String JAMMER_RID = "80014a";
    private final Integer FAMILIAR_COST = 100;
    private final Integer FAMILIAR_DISCOUNT_COST = 50;
    private final Integer FAMILIAR_DURATION = 720;

    // Jammers
    private final List<Integer> JAMMER_PACKAGE_QTYS = Arrays.asList(1, 5, 10, 25, 50, 100);
    private final Integer JAMMER_COST = 10;

    // Moods
    private final List<String> moodIds = Arrays.asList("80041a", "80040a", "80039a", "80038a", "80037a", "80024a", "80023a",
            "80022a", "80021a", "80020a", "80019a", "80018a", "80013a", "80012a", "80011a", "80010a", "80009a", "80008a");
    private final Integer MOOD_COST = 100;

    // Cleanings
    private final List<String> cleaningIds = Arrays.asList("70021a");
    private final Integer CLEANING_COST = 100;

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

    @EventListener(ApplicationReadyEvent.class)
    public void insertMoods() {
        // Iterate over the mood ids
        int idNum = 0;
        for (String id : moodIds) {
            // Attempt to get from the DB
            Mood mood = moodService.findByRid(id).orElse(null);

            // Only insert if the data is null
            if (mood == null) {
                // Build a new mood to insert
                Mood newMood = Mood.builder()
                        .id(idNum)
                        .rid(id)
                        .cost(MOOD_COST)
                        .build();

                // Save it to the db
                moodService.save(newMood);
            }

            // Increment id
            idNum++;
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void insertCleanings() {
        // Iterate over the cleaning ids
        int idNum = 0;
        for (String id : cleaningIds) {
            // Attempt to get from the DB
            Cleaning cleaning = cleaningService.findByRid(id).orElse(null);

            // Only insert if the data is null
            if (cleaning == null) {
                // Build a new mood to insert
                Cleaning newCleaning = Cleaning.builder()
                        .id(idNum)
                        .rid(id)
                        .cost(MOOD_COST)
                        .build();

                // Save it to the db
                cleaningService.save(newCleaning);
            }

            // Increment id
            idNum++;
        }
    }
}
