package com.mudosa.musinsa.coupon.domain.repository;

import com.mudosa.musinsa.coupon.domain.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    /*
    * 쿠폰을 id로 조회하면서, 동시에 쓰기 비관적 락을 걸어주는 쿼리 메서드 , 쿠폰 행만 잠금
    * */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select distinct c from Coupon c left join fetch c.couponProducts cp where c.id = :couponId")
    Optional<Coupon> findByIdForUpdate(@Param("couponId") Long couponId);


}
