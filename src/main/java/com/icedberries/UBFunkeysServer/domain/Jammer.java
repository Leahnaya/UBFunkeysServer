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
@Table(name = "Jammers")
public class Jammer {

    // DB ID
    @Id
    private Integer id;

    // Item ID
    private String rid;

    private Integer cost;

    private Integer qty;
}
