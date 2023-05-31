package com.icedberries.UBFunkeysServer.domain;

import com.icedberries.UBFunkeysServer.config.TableNames;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
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
@Table(name = TableNames.CRIB)
public class Crib {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String cribName;

    private String username;

    @Column(columnDefinition = "MEDIUMTEXT")
    @Type(type = "org.hibernate.type.TextType")
    private String profileData;
}
