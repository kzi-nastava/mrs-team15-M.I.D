package com.example.ridenow.dto.admin;

import com.example.ridenow.dto.model.PriceConfigDTO;

import java.util.List;

public class PriceConfigRequestDTO {
    private List<PriceConfigDTO> prices;

    public PriceConfigRequestDTO() {}

    public PriceConfigRequestDTO(List<PriceConfigDTO> prices) {
        this.prices = prices;
    }

    public List<PriceConfigDTO> getPrices() {
        return prices;
    }

    public void setPrices(List<PriceConfigDTO> prices) {
        this.prices = prices;
    }
}
