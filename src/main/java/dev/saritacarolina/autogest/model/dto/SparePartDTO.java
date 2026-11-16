package dev.saritacarolina.autogest.model.dto;

import java.math.BigDecimal;
import java.util.Set;

public record SparePartDTO(
        String name,
        String description,
        BigDecimal price,
        String eanCode,
        Integer stock,
        String imagePath,
        Set<Long> compatibleModelIds
) {
}