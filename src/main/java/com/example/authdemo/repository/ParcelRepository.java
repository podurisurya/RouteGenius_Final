package com.example.authdemo.repository;

import com.example.authdemo.model.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List; // ✅ ADDED: This import fixes the "cannot find symbol" error
import java.util.Optional;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {

    Optional<Parcel> findByTrackingId(String trackingId);

    // This line needed the import for "List"
    List<Parcel> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);
}
