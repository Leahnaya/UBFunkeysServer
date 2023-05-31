package com.icedberries.UBFunkeysServer.domain;

import com.icedberries.UBFunkeysServer.config.TableNames;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = TableNames.FAMILIAR)
public class Familiar {

    // DB ID
    @Id
    private Integer id;

    // Item ID
    private String rid;

    private Integer cost;

    private Integer discountedCost;

    // This is the number of hours it lasts
    private Integer duration;
}
