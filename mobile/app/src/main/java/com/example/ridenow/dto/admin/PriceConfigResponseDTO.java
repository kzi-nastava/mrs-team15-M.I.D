package com.example.ridenow.dto.admin;

import com.example.ridenow.dto.model.PriceConfigDTO;

import java.util.List;

public class PriceConfigResponseDTO {
    private List<PriceConfigDTO> prices;

    public PriceConfigResponseDTO() {}

    public PriceConfigResponseDTO(List<PriceConfigDTO> prices) {
        this.prices = prices;
    }

    public List<PriceConfigDTO> getPrices() {
        return prices;
    }

    public void setPrices(List<PriceConfigDTO> prices) {
        this.prices = prices;
    }
}
