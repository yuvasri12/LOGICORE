package com.devbridge.logicore.service.impl;

import com.devbridge.logicore.dto.AssignLocationRequest;
import com.devbridge.logicore.dto.DispatchRequest;
import com.devbridge.logicore.dto.DispatchResponse;
import com.devbridge.logicore.dto.FreightAssignmentResponse;
import com.devbridge.logicore.dto.WarehouseReceiptResponse;
import com.devbridge.logicore.dto.WarehouseReceiveRequest;
import com.devbridge.logicore.exception.ResourceNotFoundException;
import com.devbridge.logicore.model.Dispatch;
import com.devbridge.logicore.model.FreightAssignment;
import com.devbridge.logicore.model.Shipment;
import com.devbridge.logicore.model.ShipmentStatus;
import com.devbridge.logicore.model.ShipmentStatusHistory;
import com.devbridge.logicore.model.StorageLocation;
import com.devbridge.logicore.model.WarehouseReceipt;
import com.devbridge.logicore.repository.DispatchRepository;
import com.devbridge.logicore.repository.FreightAssignmentRepository;
import com.devbridge.logicore.repository.ShipmentRepository;
import com.devbridge.logicore.repository.StorageLocationRepository;
import com.devbridge.logicore.repository.WarehouseReceiptRepository;
import com.devbridge.logicore.service.WarehouseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final ShipmentRepository shipmentRepository;
    private final WarehouseReceiptRepository warehouseReceiptRepository;
    private final FreightAssignmentRepository freightAssignmentRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final DispatchRepository dispatchRepository;

    public WarehouseServiceImpl(
            ShipmentRepository shipmentRepository,
            WarehouseReceiptRepository warehouseReceiptRepository,
            FreightAssignmentRepository freightAssignmentRepository,
            StorageLocationRepository storageLocationRepository,
            DispatchRepository dispatchRepository) {
        this.shipmentRepository = shipmentRepository;
        this.warehouseReceiptRepository = warehouseReceiptRepository;
        this.freightAssignmentRepository = freightAssignmentRepository;
        this.storageLocationRepository = storageLocationRepository;
        this.dispatchRepository = dispatchRepository;
    }

    @Override
    public WarehouseReceiptResponse receive(WarehouseReceiveRequest request) {

        Shipment shipment = shipmentRepository.findById(request.getShipmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shipment not found with id " + request.getShipmentId()));

        WarehouseReceipt receipt = new WarehouseReceipt();
        receipt.setShipment(shipment);
        receipt.setCondition(request.getCondition());
        receipt.setWeightChecked(request.getWeightChecked());
        receipt.setReceivedBy(request.getReceivedBy());
        receipt.setReceivedAt(LocalDateTime.now());

        WarehouseReceipt savedReceipt = warehouseReceiptRepository.save(receipt);

        shipment.setStatus(ShipmentStatus.RECEIVED);
        shipment.addStatusHistory(buildHistory(
                ShipmentStatus.RECEIVED,
                "Freight received in condition " + request.getCondition(),
                request.getReceivedBy()));
        shipmentRepository.save(shipment);

        WarehouseReceiptResponse response = new WarehouseReceiptResponse();
        response.setId(savedReceipt.getId());
        response.setShipmentId(shipment.getId());
        response.setCondition(savedReceipt.getCondition());
        response.setWeightChecked(savedReceipt.getWeightChecked());
        response.setReceivedBy(savedReceipt.getReceivedBy());
        response.setReceivedAt(savedReceipt.getReceivedAt());

        return response;
    }

    @Override
    public FreightAssignmentResponse assignLocation(AssignLocationRequest request) {

        Shipment shipment = shipmentRepository.findById(request.getShipmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shipment not found with id " + request.getShipmentId()));

        StorageLocation location = storageLocationRepository
                .findByZoneAndRackAndBin(request.getZone(), request.getRack(), request.getBin())
                .orElseGet(() -> {
                    StorageLocation newLocation = new StorageLocation();
                    newLocation.setZone(request.getZone());
                    newLocation.setRack(request.getRack());
                    newLocation.setBin(request.getBin());
                    return storageLocationRepository.save(newLocation);
                });

        FreightAssignment assignment = new FreightAssignment();
        assignment.setShipment(shipment);
        assignment.setStorageLocation(location);
        assignment.setAssignedAt(LocalDateTime.now());

        FreightAssignment savedAssignment = freightAssignmentRepository.save(assignment);

        shipment.setStatus(ShipmentStatus.IN_WAREHOUSE);
        shipment.addStatusHistory(buildHistory(
                ShipmentStatus.IN_WAREHOUSE,
                "Assigned to zone " + request.getZone() + ", rack " + request.getRack() + ", bin " + request.getBin(),
                request.getAssignedBy()));
        shipmentRepository.save(shipment);

        FreightAssignmentResponse response = new FreightAssignmentResponse();
        response.setId(savedAssignment.getId());
        response.setShipmentId(shipment.getId());
        response.setZone(location.getZone());
        response.setRack(location.getRack());
        response.setBin(location.getBin());
        response.setAssignedAt(savedAssignment.getAssignedAt());

        return response;
    }

    @Override
    public DispatchResponse dispatch(DispatchRequest request) {

        Shipment shipment = shipmentRepository.findById(request.getShipmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shipment not found with id " + request.getShipmentId()));

        Dispatch dispatch = new Dispatch();
        dispatch.setShipment(shipment);
        dispatch.setVehicleNumber(request.getVehicleNumber());
        dispatch.setDestination(request.getDestination());
        dispatch.setDispatchedAt(LocalDateTime.now());

        Dispatch savedDispatch = dispatchRepository.save(dispatch);

        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        shipment.addStatusHistory(buildHistory(
                ShipmentStatus.IN_TRANSIT,
                "Dispatched on vehicle " + request.getVehicleNumber() + " to " + request.getDestination(),
                request.getDispatchedBy()));
        shipmentRepository.save(shipment);

        DispatchResponse response = new DispatchResponse();
        response.setId(savedDispatch.getId());
        response.setShipmentId(shipment.getId());
        response.setVehicleNumber(savedDispatch.getVehicleNumber());
        response.setDestination(savedDispatch.getDestination());
        response.setDispatchedAt(savedDispatch.getDispatchedAt());

        return response;
    }

    private ShipmentStatusHistory buildHistory(ShipmentStatus status, String remarks, String updatedBy) {
        ShipmentStatusHistory history = new ShipmentStatusHistory();
        history.setStatus(status);
        history.setRemarks(remarks);
        history.setUpdatedBy(updatedBy != null ? updatedBy : "SYSTEM");
        history.setUpdatedAt(LocalDateTime.now());
        return history;
    }
}