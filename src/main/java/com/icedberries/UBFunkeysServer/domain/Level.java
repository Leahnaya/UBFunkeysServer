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
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = TableNames.LEVEL)
public class Level {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;

    // BOMBER or RECYCLE
    private String gameName;

    private String levelName;

    @Column(columnDefinition = "MEDIUMTEXT")
    @Type(type = "org.hibernate.type.TextType")
    private String levelData;

    private LocalDateTime sharedDate;

    private String imagePath;

    // 0 -> 5 "stars"
    private double rating;

    private Integer ratingCount;

    private Integer playCount;

    // We aren't 100% sure what this was used for originally,
    // so now it will just hold the total number of stars ever received
    private Integer pos;

    public Integer getRating() {
        if (rating <= 0) {
            return 0;
        }

        double average = rating / ratingCount;
        return (int)Math.rint(average);
    }

    public Double getRatingRaw() {
        return rating;
    }
}
