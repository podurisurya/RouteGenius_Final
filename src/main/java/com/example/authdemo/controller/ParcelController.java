package com.example.authdemo.controller;

import com.example.authdemo.dto.ParcelRequest;
import com.example.authdemo.model.Parcel;
import com.example.authdemo.service.ParcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/parcels")
public class ParcelController {

    @Autowired
    private ParcelService parcelService;

    // ✅ CHANGED: The logic inside this method is now much simpler.
    @PostMapping("/add")
    public ResponseEntity<?> addParcel(@RequestBody ParcelRequest request) {
        try {
            // We directly pass the request object to the service.
            // The service now contains all the logic for validation and creation.
            Parcel savedParcel = parcelService.addParcel(request);
            return ResponseEntity.ok(savedParcel);
        } catch (IllegalArgumentException e) {
            // This catches errors like "Customer email not verified" from the service.
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("An unexpected server error occurred.");
        }
    }

    @GetMapping
    public ResponseEntity<List<Parcel>> getAllParcels() {
        return ResponseEntity.ok(parcelService.getAllParcels());
    }

    @PutMapping("/update-status")
    public ResponseEntity<?> updateStatus(@RequestParam String trackingId, @RequestParam String status) {
        Parcel updated = parcelService.updateParcelStatus(trackingId, status);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{trackingId}")
    public ResponseEntity<?> deleteParcel(@PathVariable String trackingId) {
        boolean deleted = parcelService.deleteParcelByTrackingId(trackingId);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // This method should also be reviewed. For now, the main compilation error is fixed.
    @PutMapping("/update/{trackingId}")
    public ResponseEntity<?> updateParcel(@PathVariable String trackingId, @RequestBody Parcel parcel) {
        Parcel updated = parcelService.updateParcel(trackingId, parcel);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{trackingId}")
    public ResponseEntity<?> getParcelByTrackingId(@PathVariable String trackingId) {
        return parcelService.getParcelByTrackingId(trackingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
