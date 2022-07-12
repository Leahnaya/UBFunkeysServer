package com.icedberries.UBFunkeysServer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Cribs")
public class Crib {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String cribName;

    private String username;

    private String profileData;
}
