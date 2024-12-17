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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class PointServiceTest {

    private final UserPointTable userPointTable = mock(UserPointTable.class);
    private final PointHistoryTable pointHistoryTable = mock(PointHistoryTable.class);
    private final PointValidationHandler pointValidationHandler = mock(PointValidationHandler.class);

    private final PointService pointService = new PointService(userPointTable, pointHistoryTable, pointValidationHandler);

    @Test
    void id로_특정유저의_포인트_정보를_조회한다() { // 통과
        // given
        long id = 1L;
        long point = 1000L;
        long updateMillis = 100000L;

        given(userPointTable.selectById(1L))
                .willReturn(new UserPoint(1L, 1000L, 100000L));

        // when // then
        assertThat(pointService.selectById(id))
                .extracting("id", "point", "updateMillis")
                .containsExactly(id, point, updateMillis);
    }

    @Test
    void id로_특정유저의_포인트_충전_또는_사용_내역을_조회한다() { // 통과
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
    void 포인트_충전_시에_결과값이_1_000_000원_초과일_경우_요청은_실패한다() {
        // given
        long id = 1L;
        long amount = 1_001L;
        long originUserPoint = 999_000L;

        given(userPointTable.selectById(1L))
                .willReturn(new UserPoint(1L, 999_000L, 100000L));

        given(userPointTable.insertOrUpdate(1L, 1_000_001L))
                .willReturn(new UserPoint(1L, 1_000_001L, 100000L));


        // 반환값이 void인 메서드를 스텁하려면 **doThrow(...).when(...).method() 구조 사용
        doThrow(new RuntimeException("보유 포인트는 100만원 초과일 수 없습니다."))
                .when(pointValidationHandler)
                .validateMaxPointLimit(1_001L, 999_000L);

        // when // then
        assertThatThrownBy(() -> pointService.charge(id, 1_001L))
                .isInstanceOf(RuntimeException.class);

        verify(pointValidationHandler, times(1)).validateMaxPointLimit(1_001L, 999_000L);
    }

    @Test
    void 포인트_충전_시에_결과값이_0원_이상_1_000_000원_이하일_경우_요청은_성공한다() {
        // given
        long id = 1L;
        long amount = 1000L;
        long updateMillis = 100000L;
        long originUserPoint = 999000L;
        long sumPoint = originUserPoint + amount;

        given(userPointTable.selectById(1L))
                .willReturn(new UserPoint(id, originUserPoint, updateMillis));

        given(userPointTable.insertOrUpdate(1L, sumPoint))
                .willReturn(new UserPoint(id, sumPoint, updateMillis));

        System.out.println(userPointTable.insertOrUpdate(1L, sumPoint).toString());

        /*given(userPointTable.insertOrUpdate(1L, 1000000L))
                .willReturn(new UserPoint(id, originUserPoint + amount, updateMillis));*/



        // when // then
        /*assertThat(pointService.charge(id, amount))
                .extracting("id", "point", "updateMillis")
                .containsExactly(id, sumPoint , updateMillis);*/
    }

    @Test
    void 포인트_사용_시에_결과값이_0원_미만일_경우_요청은_실패한다() {
        // given
        long id = 1L;
        long amount = 1001L;
        long originUserPoint = 1000L;
        long updateMillis = 100000L;

        given(userPointTable.selectById(1L))
                .willReturn(new UserPoint(id, originUserPoint, updateMillis));

        doThrow(new RuntimeException("보유 포인트는 0원 이하일 수 없습니다."))
                .when(pointValidationHandler)
                .validateMinPointLimit(1001L, 1000L);

        // when // then
        assertThrows(RuntimeException.class, () -> {
            pointService.use(id, amount);
        });
    }

    @Test
    void 포인트_사용_시에_결과값이_0원_이상일_경우_요청은_성공한다() {
        // given
        long id = 1L;
        long amount = 1000L;
        long updateMillis = 100000L;
        long originUserPoint = 1000L;
        long subtractPoint = originUserPoint - amount;

        given(userPointTable.selectById(1L))
                .willReturn(new UserPoint(id, originUserPoint, updateMillis));

        given(userPointTable.insertOrUpdate(id, subtractPoint))
                .willReturn(new UserPoint(id, subtractPoint, updateMillis));

        // when // then
        assertThat(pointService.use(id, amount))
                .extracting("id", "point", "updateMillis")
                .containsExactly(id, subtractPoint , updateMillis);
    }

    @Test
    void 포인트_충전_시에_충전_요청값이_0원_이하일_경우_요청은_실패한다() {
        // given
        long id = 1L;
        long amount = 0L;

        doThrow(new RuntimeException("충전 요청 포인트는 0원 이하일 수 없습니다."))
                .when(pointValidationHandler)
                .validateChargePointAboveZero(amount);

        // when // then
        assertThrows(RuntimeException.class, () -> {
            pointService.charge(id, amount);
        });
    }

//    @Test
//    void 포인트_충전_시에_충전_요청값이_1원_이상일_경우_요청은_성공한다() {
//
//    }

    @Test
    void 포인트_사용_시에_사용_요청값이_0원_이하일_경우_요청은_실패한다() {
        // given
        long id = 1L;
        long amount = 0L;

        doThrow(new RuntimeException("사용 요청 포인트는 0원 이하일 수 없습니다."))
                .when(pointValidationHandler)
                .validateUsePointAboveZero(amount);

        // when // then
        assertThrows(RuntimeException.class, () -> {
            pointService.use(id, amount);
        });

        verify(pointValidationHandler).validateUsePointAboveZero(amount);
    }

//    @Test
//    void 포인트_사용_시에_사용_요청값이_1원_이상일_경우_요청은_성공한다() {
//
//    }
}