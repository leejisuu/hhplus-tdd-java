package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component
public class PointValidationHandler {

    private final String ERROR_MAX_POINT = "보유 포인트는 100만원 이상일 수 없습니다.";
    private final String ERROR_MIN_POINT = "보유 포인트는 0원 이하일 수 없습니다.";
    private final String ERROR_CHARGE_POINT = "충전 요청 포인트는 0원 이하일 수 없습니다.";
    private final String ERROR_USE_POINT = "사용 요청 포인트는 0원 이하일 수 없습니다.";

    private final long MAX_AVAILABLE_POINT = 1_000_000L;

    public void validateMaxPointLimit(long amount, long originPoint) {
        if(originPoint + amount > MAX_AVAILABLE_POINT) {
            throw new RuntimeException(ERROR_MAX_POINT);
        }
    }

    public void validateMinPointLimit(long amount, long originPoint) {
        if(originPoint - amount < 0) {
            throw new RuntimeException(ERROR_MIN_POINT);
        }
    }

    public void validateChargePointAboveZero(long amount) {
        if(amount <= 0) {
            throw new RuntimeException(ERROR_CHARGE_POINT);
        }
    }

    public void validateUsePointAboveZero(long amount) {
        if(amount <= 0) {
            throw new RuntimeException(ERROR_USE_POINT);
        }
    }
}
