package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

}
