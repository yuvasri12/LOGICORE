package com.devbridge.logicore.service;

import com.devbridge.logicore.dto.*;
import com.devbridge.logicore.model.ShipmentMode;
import com.devbridge.logicore.model.ShipmentStatus;

import java.util.List;

public interface ShipmentService {

    ShipmentResponse createShipment(CreateShipmentRequest request);

    List<ShipmentResponse> listShipments(
            ShipmentStatus status,
            ShipmentMode mode,
            String customerName);

    ShipmentResponse getShipment(Long id);

    ShipmentResponse updateShipment(
            Long id,
            UpdateShipmentRequest request);

    ShipmentResponse updateStatus(
            Long id,
            UpdateStatusRequest request);

    ShipmentResponse cancelShipment(
            Long id,
            CancelShipmentRequest request);

    TrackingResponse getTracking(String trackingNumber);
}