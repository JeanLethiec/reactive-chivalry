package com.ceihtel.chivalry.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Soldier {
    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String weapon;
}
