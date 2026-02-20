package com.spartaifive.commercepayment.domain.point.repository;

import com.spartaifive.commercepayment.domain.point.entity.PointAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointAuditRepository extends JpaRepository<PointAudit, Long> {
}
