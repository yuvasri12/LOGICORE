package com.devbridge.logicore.service.impl;

import com.devbridge.logicore.dto.CancelShipmentRequest;
import com.devbridge.logicore.dto.CreateShipmentRequest;
import com.devbridge.logicore.dto.ShipmentResponse;
import com.devbridge.logicore.dto.StatusHistoryResponse;
import com.devbridge.logicore.dto.TrackingResponse;
import com.devbridge.logicore.dto.TrackingTimelineResponse;
import com.devbridge.logicore.dto.UpdateShipmentRequest;
import com.devbridge.logicore.dto.UpdateStatusRequest;
import com.devbridge.logicore.exception.ResourceNotFoundException;
import com.devbridge.logicore.model.Shipment;
import com.devbridge.logicore.model.ShipmentMode;
import com.devbridge.logicore.model.ShipmentStatus;
import com.devbridge.logicore.model.ShipmentStatusHistory;
import com.devbridge.logicore.repository.ShipmentRepository;
import com.devbridge.logicore.service.ShipmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;

    public ShipmentServiceImpl(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    public ShipmentResponse createShipment(CreateShipmentRequest request) {

        Shipment shipment = new Shipment();

        shipment.setTrackingNumber("TRK-" + System.currentTimeMillis());
        shipment.setCustomerName(request.getCustomerName());
        shipment.setOrigin(request.getOrigin());
        shipment.setDestination(request.getDestination());
        shipment.setMode(request.getMode());
        shipment.setWeight(request.getWeight());
        shipment.setStatus(ShipmentStatus.NEW);

        shipment.addStatusHistory(buildHistory(ShipmentStatus.NEW, "Shipment created", "SYSTEM"));

        Shipment savedShipment = shipmentRepository.save(shipment);

        return mapToResponse(savedShipment);
    }

    @Override
    public List<ShipmentResponse> listShipments(
            ShipmentStatus status,
            ShipmentMode mode,
            String customerName) {

        List<Shipment> shipments = shipmentRepository.findAll();
        List<ShipmentResponse> responses = new ArrayList<ShipmentResponse>();

        for (Shipment shipment : shipments) {
            if (status != null && shipment.getStatus() != status) {
                continue;
            }
            if (mode != null && shipment.getMode() != mode) {
                continue;
            }
            if (customerName != null && !customerName.trim().isEmpty()
                    && (shipment.getCustomerName() == null
                    || !shipment.getCustomerName().toLowerCase().contains(customerName.toLowerCase()))) {
                continue;
            }
            responses.add(mapToResponse(shipment));
        }

        return responses;
    }

    @Override
    public ShipmentResponse getShipment(Long id) {
        Shipment shipment = findShipmentOrThrow(id);
        return mapToResponse(shipment);
    }

    @Override
    public ShipmentResponse updateShipment(Long id, UpdateShipmentRequest request) {

        Shipment shipment = findShipmentOrThrow(id);

        shipment.setCustomerName(request.getCustomerName());
        shipment.setOrigin(request.getOrigin());
        shipment.setDestination(request.getDestination());
        shipment.setMode(request.getMode());
        shipment.setWeight(request.getWeight());

        Shipment updatedShipment = shipmentRepository.save(shipment);

        return mapToResponse(updatedShipment);
    }

    @Override
    public ShipmentResponse updateStatus(Long id, UpdateStatusRequest request) {

        Shipment shipment = findShipmentOrThrow(id);

        shipment.setStatus(request.getStatus());
        shipment.addStatusHistory(buildHistory(
                request.getStatus(),
                request.getRemarks(),
                request.getUpdatedBy()));

        Shipment updatedShipment = shipmentRepository.save(shipment);

        return mapToResponse(updatedShipment);
    }

    @Override
    public ShipmentResponse cancelShipment(Long id, CancelShipmentRequest request) {

        Shipment shipment = findShipmentOrThrow(id);

        shipment.setStatus(ShipmentStatus.CANCELLED);
        shipment.addStatusHistory(buildHistory(
                ShipmentStatus.CANCELLED,
                request.getReason(),
                request.getCancelledBy()));

        Shipment cancelledShipment = shipmentRepository.save(shipment);

        return mapToResponse(cancelledShipment);
    }

    @Override
    public TrackingResponse getTracking(String trackingNumber) {

        Shipment shipment = shipmentRepository
                .findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shipment not found with tracking number " + trackingNumber));

        TrackingResponse response = new TrackingResponse();
        response.setTrackingNumber(shipment.getTrackingNumber());
        response.setOrigin(shipment.getOrigin());
        response.setDestination(shipment.getDestination());
        response.setMode(shipment.getMode());
        response.setCurrentStatus(shipment.getStatus());

        List<TrackingTimelineResponse> timeline = new ArrayList<TrackingTimelineResponse>();
        for (ShipmentStatusHistory history : shipment.getStatusHistory()) {
            TrackingTimelineResponse entry = new TrackingTimelineResponse();
            entry.setStatus(history.getStatus());
            entry.setPublicRemark(history.getRemarks());
            entry.setUpdatedAt(history.getUpdatedAt());
            timeline.add(entry);
        }
        response.setTimeline(timeline);

        return response;
    }

    private Shipment findShipmentOrThrow(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id " + id));
    }

    private ShipmentStatusHistory buildHistory(ShipmentStatus status, String remarks, String updatedBy) {
        ShipmentStatusHistory history = new ShipmentStatusHistory();
        history.setStatus(status);
        history.setRemarks(remarks);
        history.setUpdatedBy(updatedBy != null ? updatedBy : "SYSTEM");
        history.setUpdatedAt(LocalDateTime.now());
        return history;
    }

    private ShipmentResponse mapToResponse(Shipment shipment) {
        ShipmentResponse response = new ShipmentResponse();

        response.setId(shipment.getId());
        response.setTrackingNumber(shipment.getTrackingNumber());
        response.setCustomerName(shipment.getCustomerName());
        response.setOrigin(shipment.getOrigin());
        response.setDestination(shipment.getDestination());
        response.setMode(shipment.getMode());
        response.setWeight(shipment.getWeight());
        response.setStatus(shipment.getStatus());
        response.setCreatedAt(shipment.getCreatedAt());
        response.setUpdatedAt(shipment.getUpdatedAt());

        List<StatusHistoryResponse> timeline = new ArrayList<StatusHistoryResponse>();
        for (ShipmentStatusHistory history : shipment.getStatusHistory()) {
            StatusHistoryResponse entry = new StatusHistoryResponse();
            entry.setId(history.getId());
            entry.setStatus(history.getStatus());
            entry.setRemarks(history.getRemarks());
            entry.setUpdatedBy(history.getUpdatedBy());
            entry.setUpdatedAt(history.getUpdatedAt());
            timeline.add(entry);
        }
        response.setTimeline(timeline);

        return response;
    }
}