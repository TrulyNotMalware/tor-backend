package com.hackerton.tor.torback.repository;

import com.hackerton.tor.torback.entity.Purchase_history;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface PurchaseRepository extends R2dbcRepository<Purchase_history, String> {
}
