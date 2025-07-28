package com.example.authdemo.controller;

import com.example.authdemo.model.Parcel;
import com.example.authdemo.service.ParcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parcels") // A new, non-admin path
public class PublicParcelController {

    @Autowired
    private ParcelService parcelService;

    // This is the new public endpoint for tracking
    @GetMapping("/track/{trackingId}")
    public ResponseEntity<?> getParcelByTrackingId(@PathVariable String trackingId) {
        return parcelService.getParcelByTrackingId(trackingId)
                .map(ResponseEntity::ok) // If found, return parcel data with 200 OK
                .orElse(ResponseEntity.notFound().build()); // If not found, return 404 Not Found
    }
}
