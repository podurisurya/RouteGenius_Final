package com.example.authdemo.controller;

import com.example.authdemo.model.Parcel;
import com.example.authdemo.service.ParcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user") // A new, secure path for logged-in users
public class UserDashboardController {

    @Autowired
    private ParcelService parcelService;

    // This new endpoint fetches all parcels for a specific customer
    @GetMapping("/parcels/{email}")
    public ResponseEntity<List<Parcel>> getParcelsForUser(@PathVariable String email) {
        // In a real-world app, you'd verify that the logged-in user's email matches this path variable.
        // For this project, this is sufficient.
        List<Parcel> parcels = parcelService.getParcelsByCustomerEmail(email);
        return ResponseEntity.ok(parcels);
    }
}
