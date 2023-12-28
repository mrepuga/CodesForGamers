package com.example.gamecatalog.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;


@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "CodeGameItem")
@JsonIgnoreProperties({"game"})
public class CodeGameItem {
    @Id
    @NotBlank
    @Column(unique = true)
    private String code;

    @Column
    @NotBlank
    private String platform;

    @ManyToOne
    private Game game;

    @Column
    private Boolean selected;
}
