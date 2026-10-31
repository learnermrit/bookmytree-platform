package com.bookmytree.platform.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenameFieldRequestDto {
    @NotBlank(message = "Field name is required.")
    private String name;
}
