package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    // [정책] 유저가 보유 할 수 있는 최대 포인트는 100만 포인트이다.
    private final long MAX_AVAILABLE_POINT = 1_000_000L;

    public UserPoint selectUserPointById(long id) {

        return userPointTable.selectById(id);
    }

    public List<PointHistory> selectPointHistoryListByUserId(long id) {

        return pointHistoryTable.selectAllByUserId(id);
    }

    public UserPoint chargeUserPoint(long id, long amount, long updateMillis) {
        if(amount <= 0) {
            throw new RuntimeException("충전 요청 금액은 0원보다 커야 합니다.");
        }
        if(amount > MAX_AVAILABLE_POINT) {
            throw new RuntimeException("충전 요청 금액은 최대 100만원을 초과할 수 없습니다.");
        }

        UserPoint originUserPoint = userPointTable.selectById(id);
        if(originUserPoint.point() + amount > MAX_AVAILABLE_POINT) {
            throw new RuntimeException("충전 후 보유 포인트는 최대 100만원을 초과할 수 없습니다.");
        }

        UserPoint chargedUserPoint = userPointTable.insertOrUpdate(id, originUserPoint.point() + amount);

        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, updateMillis);

        return chargedUserPoint;
    }

    public UserPoint useUserPoint(long id, long amount, long updateMillis) {
        if(amount <= 0) {
            throw new RuntimeException("사용 요청 금액은 0원보다 커야 합니다.");
        }
        if(amount > MAX_AVAILABLE_POINT) {
            throw new RuntimeException("사용 요청 금액은 최대 100만원을 초과할 수 없습니다.");
        }

        UserPoint originUserPoint = userPointTable.selectById(id);
        if(originUserPoint.point() - amount < 0) {
            throw new RuntimeException("사용하려는 포인트는 보유한 포인트보다 클 수 없습니다.");
        }

        UserPoint usedUserPoint = userPointTable.insertOrUpdate(id, originUserPoint.point() - amount);

        pointHistoryTable.insert(id, amount, TransactionType.USE, updateMillis);

        return usedUserPoint;
    }
}
