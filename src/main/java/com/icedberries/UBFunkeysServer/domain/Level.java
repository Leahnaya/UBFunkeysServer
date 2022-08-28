package com.icedberries.UBFunkeysServer.domain;

import com.icedberries.UBFunkeysServer.config.TableNames;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
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

    private Integer pos;

    public Integer getRating() {
        if (rating <= 0) {
            return 0;
        }

        double average = rating / ratingCount;
        return (int)Math.rint(average);
    }
}
