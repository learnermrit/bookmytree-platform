package com.bookmytree.platform.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenameTreeRequestDto {
    @NotBlank(message = "Tree species is required.")
    private String species;

    private String stage;
}
