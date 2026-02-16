package com.spartaifive.commercepayment.domain.point.entity;

import lombok.Getter;

@Getter
public enum PointStatus {
    NOT_READY_TO_BE_SPENT,
    VOIDED,
    CAN_BE_SPENT,
}
