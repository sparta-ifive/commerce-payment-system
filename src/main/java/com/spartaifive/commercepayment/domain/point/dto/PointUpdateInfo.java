package com.spartaifive.commercepayment.domain.point.dto;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.point.entity.Point;
import com.spartaifive.commercepayment.domain.user.entity.MembershipGrade;

public record PointUpdateInfo (
        Point point,
        Payment payment,
        MembershipGrade membershipGrade
){
}
