package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.apache.catalina.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class PointServiceTest {

    private final UserPointTable userPointTable = mock(UserPointTable.class);
    private final PointHistoryTable pointHistoryTable = mock(PointHistoryTable.class);
    private final PointValidationHandler pointValidationHandler = mock(PointValidationHandler.class);

    private final PointService pointService = new PointService(userPointTable, pointHistoryTable, pointValidationHandler);

    @Test
    void id로_특정유저의_포인트_정보를_조회한다() {
        // given
        long id = 1L;
        long point = 1000L;
        long updateMillis = 100000L;

        given(userPointTable.selectById(1L))
                .willReturn(new UserPoint(1L, 1000L, 100000L));

        // when // then
        assertThat(userPointTable.selectById(id))
                .extracting("id", "point", "updateMillis")
                .contains(id, point, updateMillis);
    }

    @Test
    void id로_특정유저의_포인트_충전_또는_사용_내역을_조회한다() {
        // given
        long userId = 1L;

        given(pointHistoryTable.selectAllByUserId(userId))
            .willReturn(List.of(
                new PointHistory(1L, 1L, 100L, TransactionType.CHARGE, 10000L),
                new PointHistory(2L, 1L, 50L, TransactionType.USE, 10005L),
                new PointHistory(3L, 1L, 70L, TransactionType.CHARGE, 10010L),
                new PointHistory(4L, 1L, 80L, TransactionType.USE, 10015L)
        ));

        // when // then
        assertThat(pointService.selectAllByUserId(userId)).hasSize(4)
                .extracting("id", "userId", "amount", "type", "updateMillis")
                .containsExactly(
                        tuple(1L, 1L, 100L, TransactionType.CHARGE, 10000L),
                        tuple(2L, 1L, 50L, TransactionType.USE, 10005L),
                        tuple(3L, 1L, 70L, TransactionType.CHARGE, 10010L),
                        tuple(4L, 1L, 80L, TransactionType.USE, 10015L)
                );
    }

    @Test
    void 포인트_충전_시에_결과값이_1_000_000_초과일_경우_요청은_실패한다() {
        // given

        // when

        // then
    }

    @Test
    void 포인트_충전_시에_결과값이_0_이상_1_000_000_이하일_경우_요청은_성공한다() {
        // given

        // when

        // then
    }

    @Test
    void 포인트_사용_시에_결과값이_0_미만일_경우_요청은_실패한다() {
        // given

        // when

        // then
    }

    @Test
    void 포인트_사용_시에_결과값이_0_이상일_경우_요청은_성공한다() {
        // given

        // when

        // then
    }
}