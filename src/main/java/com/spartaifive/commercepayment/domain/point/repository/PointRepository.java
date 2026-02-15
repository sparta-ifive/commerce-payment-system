package com.spartaifive.commercepayment.domain.point.repository;

import com.spartaifive.commercepayment.domain.point.entity.Point;
import com.spartaifive.commercepayment.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointRepository extends JpaRepository<Point, Long> {
    List<Point> findPointByOwnerUser(User ownerUser);

    List<Point> findPointByOwnerUser_Id(Long ownerUserId);
}
