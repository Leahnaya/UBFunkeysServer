package com.icedberries.UBFunkeysServer.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Familiars")
public class Familiar {

    // Item ID
    @Id
    private String id;

    private Integer cost;

    private Integer discountedCost;

    // This is the number of hours it lasts
    private Integer duration;
}
