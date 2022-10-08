package com.ceihtel.chivalry.requests;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateSoldierDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String weapon;
}
