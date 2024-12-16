package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component
public class PointValidationHandler {

    private final UserPointTable userPointTable;

    public PointValidationHandler(UserPointTable userPointTable) {
        this.userPointTable = userPointTable;
    }

    private final long MAX_AVAILABLE_POINT = 1000000;

    public void validateMaxPointLimit(long id, long amount) {
        UserPoint originUserPoint = userPointTable.selectById(id);
        if(originUserPoint.point() + amount > MAX_AVAILABLE_POINT) {
            throw new RuntimeException("보유 포인트는 100만원 이상일 수 없습니다.");
        }
    }

    public void validateMinPointLimit(long id, long amount) {
        UserPoint originUserPoint = userPointTable.selectById(id);
        if(originUserPoint.point() - amount < 0) {
            throw new RuntimeException("보유 포인트는 0원 이하일 수 없습니다.");
        }
    }

    /*
    * TODO - 테스트 케이스 작성하기
    * */
    public void validatePointAboveZero(long amount) {
        if(amount <= 0) {
            throw new RuntimeException("충전/사용 요청값은 0원 이하일 수 없습니다.");
        }
    }
}
