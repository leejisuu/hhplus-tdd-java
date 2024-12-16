package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class PointServiceTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointHistoryTable pointHistoryTable;

    @DisplayName("id로 유저의 포인트 정보를 조회한다.")
    @Test
    void selectById() {
        // given
        long id = 1;
        long point = 10;
        userPointTable.insertOrUpdate(id, point);

        // when
        UserPoint selectedUserPoint = pointService.selectById(id);

        // then
        assertThat(selectedUserPoint)
                .extracting("id", "point")
                .contains(id, point);
    }

    @DisplayName("id로 유저의 포인트 충전/사용 내역을 조회한다.")
    @Test
    void selectAllByUserId() {
        // given
        long currentTimeMillis = System.currentTimeMillis();

        pointHistoryTable.insert(1L, 10L, TransactionType.CHARGE, currentTimeMillis);
        pointHistoryTable.insert(1L, 5L, TransactionType.USE, currentTimeMillis+5);
        pointHistoryTable.insert(1L, 20L, TransactionType.CHARGE, currentTimeMillis+10);
        pointHistoryTable.insert(1L, 15L, TransactionType.USE, currentTimeMillis+15);

        // when // then
        assertThat(pointService.selectAllByUserId(1)).hasSize(4)
                .extracting("userId", "amount", "type", "updateMillis")
                .containsExactly(
                        tuple(1L, 10L, TransactionType.CHARGE, currentTimeMillis),
                        tuple(1L, 5L, TransactionType.USE, currentTimeMillis+5),
                        tuple(1L, 20L, TransactionType.CHARGE, currentTimeMillis+10),
                        tuple(1L, 15L, TransactionType.USE, currentTimeMillis+15)
                );
    }
}