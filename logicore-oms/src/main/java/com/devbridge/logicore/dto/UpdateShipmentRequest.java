package com.devbridge.logicore.dto;

import com.devbridge.logicore.model.ShipmentMode;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class UpdateShipmentRequest {

    @NotBlank
    private String customerName;

    @NotBlank
    private String origin;

    @NotBlank
    private String destination;

    @NotNull
    private ShipmentMode mode;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal weight;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public ShipmentMode getMode() {
        return mode;
    }

    public void setMode(ShipmentMode mode) {
        this.mode = mode;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
}
