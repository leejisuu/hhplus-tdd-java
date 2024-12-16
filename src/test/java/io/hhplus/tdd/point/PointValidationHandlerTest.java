package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class PointValidationHandlerTest {

    private final UserPointTable userPointTable = mock(UserPointTable.class);

    private final PointValidationHandler pointValidationHandler = new PointValidationHandler(userPointTable);

    @Test
    void 포인트_충전_시에_결과값이_1_000_000_초과일_경우_검증이_실패한다() {
        // given
        long userId = 1L;
        long amount = 999000L;

        given(userPointTable.selectById(1L))
                .willReturn(new UserPoint(1L, 1001L, 100000L));


        // when // then
        assertThrows(
                RuntimeException.class,
                () -> pointValidationHandler.validateMaxPointLimit(userId, amount)
        );
    }

    @Test
    void 포인트_충전_시에_결과값이_0_이상_1_000_000_이하일_경우_검증이_통과된다() {
        // given
        long userId = 1L;
        long amount = 999000L;

        given(userPointTable.selectById(1L))
                .willReturn(new UserPoint(1L, 1000L, 100000L));


        // when // then
        pointValidationHandler.validateMaxPointLimit(userId, amount);
    }


    @Test
    void 포인트_사용_시에_결과값이_0_미만일_경우_검증이_실패한다() {
        // given
        long userId = 1L;
        long amount = 1001L;

        given(userPointTable.selectById(1L))
                .willReturn(new UserPoint(1L, 1000L, 100000L));

        // when // then
        assertThrows(
                RuntimeException.class,
                () -> pointValidationHandler.validateMinPointLimit(userId, amount)
        );
    }

    @Test
    void 포인트_사용_시에_결과값이_0_이상일_경우_검증이_통과된다() {
        // given
        long userId = 1L;
        long amount = 1001L;

        given(userPointTable.selectById(1L))
                .willReturn(new UserPoint(1L, 1001L, 100000L));

        // when // then
        pointValidationHandler.validateMinPointLimit(userId, amount);
    }


}