package com.ceihtel.chivalry.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Soldier {
    @Id
    private Long id;

    private String name;

    private String weapon;
}
