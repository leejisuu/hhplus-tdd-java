package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final PointValidationHandler pointValidationHandler;

    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable, PointValidationHandler pointValidationHandler) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
        this.pointValidationHandler = pointValidationHandler;
    }

    public UserPoint selectById(long id) {

        return userPointTable.selectById(id);
    }

    public List<PointHistory> selectAllByUserId(long id) {

        return pointHistoryTable.selectAllByUserId(id);
    }

    public UserPoint charge(long id, long amount) {
        pointValidationHandler.validatePointAboveZero(amount);
        pointValidationHandler.validateMaxPointLimit(id, amount);

        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return userPointTable.insertOrUpdate(id, amount);
    }

    public UserPoint use(long id, long amount) {
        pointValidationHandler.validatePointAboveZero(amount);
        pointValidationHandler.validateMinPointLimit(id, amount);

        pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());

        return userPointTable.insertOrUpdate(id, amount);
    }
}
