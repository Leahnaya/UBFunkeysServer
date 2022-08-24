package com.icedberries.UBFunkeysServer.domain;

import com.icedberries.UBFunkeysServer.config.TableNames;
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
@Table(name = TableNames.JAMMER)
public class Jammer {

    // DB ID
    @Id
    private Integer id;

    // Item ID
    private String rid;

    private Integer cost;

    private Integer qty;
}
