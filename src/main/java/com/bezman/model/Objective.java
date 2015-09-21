package com.bezman.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Objective extends BaseModel
{
    private int id;

    private Integer orderID;

    @JsonIgnore
    private Game game;

    private String objectiveText;
}
