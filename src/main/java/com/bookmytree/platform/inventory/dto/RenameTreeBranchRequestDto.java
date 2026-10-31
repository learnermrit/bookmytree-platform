package com.bookmytree.platform.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenameTreeBranchRequestDto {
    @NotBlank(message = "Branch identifier is required.")
    private String branchIdentifier;
}
