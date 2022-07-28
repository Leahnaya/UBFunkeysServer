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
@Table(name = "Cleanings")
public class Cleaning {

    // DB ID
    @Id
    private Integer id;

    // Item ID
    private String rid;

    private Integer cost;
}
