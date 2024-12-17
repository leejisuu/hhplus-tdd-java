package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class PointValidationHandlerTest {

    private final PointValidationHandler pointValidationHandler = new PointValidationHandler();

    @Test
    void 포인트_충전_시에_결과값이_1_000_000원_초과일_경우_포인트_유효성_검증이_실패한다() {
        // given
        long originUserPoint = 1001L;
        long amount = 999000L;

        // when // then
        assertThrows(
                RuntimeException.class,
                () -> pointValidationHandler.validateMaxPointLimit(amount, originUserPoint)
        );
    }

    @Test
    void 포인트_충전_시에_결과값이_0원_이상_1_000_000원_이하일_경우_포인트_유효성_검증이_통과된다() {
        // given
        long originUserPoint = 1000L;
        long amount = 999000L;

        // when // then
        pointValidationHandler.validateMaxPointLimit(amount, originUserPoint);
    }


    @Test
    void 포인트_사용_시에_사용_요청_포인트가_0원_미만일_경우_포인트_유효성_검증이_실패한다() {
        // given
        long originUserPoint = 1000L;
        long amount = 1001L;

        // when // then
        assertThrows(
                RuntimeException.class,
                () -> pointValidationHandler.validateMinPointLimit(amount, originUserPoint)
        );
    }

    @Test
    void 포인트_사용_시에_결과값이_0원_이상일_경우_포인트_유효성_검증이_통과된다() {
        // given
        long originUserPoint = 1000L;
        long amount = 1000L;

        // when // then
        pointValidationHandler.validateMinPointLimit(amount, originUserPoint);
    }

    @Test
    void 포인트_충전_시에_충전_요청_포인트가_0원_이하면_포인트_유효성_검증이_실패한다() {
        // given
        long amount = 0L;

        // when // then
        assertThrows(
                RuntimeException.class,
                () -> pointValidationHandler.validateChargePointAboveZero(amount)
        );
    }

    @Test
    void 포인트_충전_시에_충전_요청_포인트가_1원_이상이면_포인트_유효성_검증이_통과한다() {
        // given
        long amount = 1L;

        // when // then
        pointValidationHandler.validateChargePointAboveZero(amount);
    }

    @Test
    void 포인트_사용_시에_사용_요청_포인트가_0원_이하면_포인트_유효성_검증이_실패한다() {
        // given
        long amount = 0L;

        // when // then
        assertThrows(
                RuntimeException.class,
                () -> pointValidationHandler.validateUsePointAboveZero(amount)
        );
    }

    @Test
    void 포인트_사용_시에_사용_요청_포인트가_1원_이상이면_포인트_유효성_검증이_통과한다() {
        // given
        long amount = 1L;

        // when // then
        pointValidationHandler.validateUsePointAboveZero(amount);
    }

}