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
    private final PointValidationHandler pointValidationHandler;

    public UserPoint selectById(long id) {

        return userPointTable.selectById(id);
    }

    public List<PointHistory> selectAllByUserId(long id) {

        return pointHistoryTable.selectAllByUserId(id);
    }

    public UserPoint charge(long id, long amount) {
       pointValidationHandler.validateChargePointAboveZero(amount);

        UserPoint originUserPoint = userPointTable.selectById(id);
        pointValidationHandler.validateMaxPointLimit(amount, originUserPoint.point());

        // pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return userPointTable.insertOrUpdate(id, originUserPoint.point() + amount);
    }

    public UserPoint use(long id, long amount) {
        pointValidationHandler.validateUsePointAboveZero(amount);

        UserPoint originUserPoint = userPointTable.selectById(id);
        pointValidationHandler.validateMinPointLimit(amount, originUserPoint.point());

        // pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());

        return userPointTable.insertOrUpdate(id, originUserPoint.point() - amount);
    }
}
