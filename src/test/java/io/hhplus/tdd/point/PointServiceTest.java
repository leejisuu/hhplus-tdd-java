package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class PointServiceTest {

    private final UserPointTable userPointTable = mock(UserPointTable.class);
    private final PointHistoryTable pointHistoryTable = mock(PointHistoryTable.class);

    private final PointService pointService = new PointService(userPointTable, pointHistoryTable);

    @Test
    void id로_특정_유저의_포인트_정보를_조회한다() { // 통과
        // given
        long id = 1L;
        long point = 1000L;
        long updateMillis = 100000L;

        given(userPointTable.selectById(id))
                .willReturn(new UserPoint(id, point, updateMillis));

        // when // then
        assertThat(pointService.selectUserPointById(id))
                .extracting("id", "point", "updateMillis")
                .containsExactly(id, point, updateMillis);

        verify(userPointTable, times(1)).selectById(id);
    }

    @Test
    void id로_특정_유저의_포인트_충전_또는_사용_내역을_조회한다() { // 통과
        // given
        long userId = 1L;

        given(pointHistoryTable.selectAllByUserId(userId))
            .willReturn(List.of(
                new PointHistory(1L, userId, 100L, TransactionType.CHARGE, 10000L),
                new PointHistory(2L, userId, 50L, TransactionType.USE, 10005L),
                new PointHistory(3L, userId, 70L, TransactionType.CHARGE, 10010L),
                new PointHistory(4L, userId, 80L, TransactionType.USE, 10015L)
        ));

        // when // then
        assertThat(pointService.selectPointHistoryListByUserId(userId)).hasSize(4)
                .extracting("id", "userId", "amount", "type", "updateMillis")
                .containsExactly(
                        tuple(1L, userId, 100L, TransactionType.CHARGE, 10000L),
                        tuple(2L, userId, 50L, TransactionType.USE, 10005L),
                        tuple(3L, userId, 70L, TransactionType.CHARGE, 10010L),
                        tuple(4L, userId, 80L, TransactionType.USE, 10015L)
                );

        verify(pointHistoryTable, times(1)).selectAllByUserId(userId);
    }

    @Test
    void 포인트_충전_시_충전_금액이_0원보다_작으면_예외를_발생한다() {
        long id = 1L;
        long willChargePoint = 0L;
        long updateMillis = 100000L;

        // when // then
        assertThatThrownBy(() -> pointService.chargeUserPoint(id, willChargePoint, updateMillis))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("충전 요청 금액은 0원보다 커야 합니다.");

        verify(userPointTable, never()).selectById(id);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
    }

    @Test
    void 포인트_충전_시에_충전_요청값이_1원보다_커야한다() {
        // given
        long id = 1L;
        long willChargePoint = 1L;
        long originPoint = 1000L;
        long updateMillis = 100000L;
        long chargeResultPoint = originPoint + willChargePoint;

        given(userPointTable.selectById(id))
                .willReturn(new UserPoint(id, originPoint, updateMillis));

        given(userPointTable.insertOrUpdate(id, chargeResultPoint))
                .willReturn(new UserPoint(id, chargeResultPoint, updateMillis));

        assertThat(pointService.chargeUserPoint(id, willChargePoint, updateMillis))
                .extracting("id", "point", "updateMillis")
                .containsExactly(id, chargeResultPoint, updateMillis);

        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, times(1)).insertOrUpdate(id, chargeResultPoint);
    }

    @Test
    void 포인트_충전_시에_충전_요청값이_1_000_000원보다_크면_예외를_발생한다() {
        // given
        long id = 1L;
        long willChargePoint = 1_000_001L;
        long updateMillis = 100000L;

        // when // then
        assertThatThrownBy(() -> pointService.chargeUserPoint(id, willChargePoint, updateMillis))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("충전 요청 금액은 최대 100만원을 초과할 수 없습니다.");

        verify(userPointTable, never()).selectById(id);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
    }

    @Test
    void 포인트_충전_시에_충전_요청값이_1_000_000원_이하여야_포인트를_저장한다() {
        // given
        long id = 1L;
        long willChargePoint = 1_000_000L;
        long originPoint = 0;
        long updateMillis = 100000L;
        long chargeResultPoint = originPoint + willChargePoint;

        given(userPointTable.selectById(id))
                .willReturn(new UserPoint(id, originPoint, updateMillis));

        given(userPointTable.insertOrUpdate(id, chargeResultPoint))
                .willReturn(new UserPoint(id, chargeResultPoint, updateMillis));

        assertThat(pointService.chargeUserPoint(id, willChargePoint, updateMillis))
                .extracting("id", "point", "updateMillis")
                .containsExactly(id, chargeResultPoint, updateMillis);

        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, times(1)).insertOrUpdate(id, chargeResultPoint);
    }

    @Test
    void 포인트_충전_시에_포인트를_합산한_결과값이_1_000_000원보다_크면_예외를_발생한다() {
        // given
        long id = 1L;
        long willChargePoint = 1_001L;
        long originPoint = 999_000L;
        long updateMillis = 100000L;

        given(userPointTable.selectById(1L))
                .willReturn(new UserPoint(1L, originPoint, 100000L));

        assertThatThrownBy(() -> pointService.chargeUserPoint(id, willChargePoint, updateMillis))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("충전 후 보유 포인트는 최대 100만원을 초과할 수 없습니다.");

        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, never()).insertOrUpdate(id, willChargePoint);
    }

    @Test
    void 포인트_충전_시에_포인트를_합산한_결과값이_1_000_000원_이하여야_포인트를_저장한다() {
        // given
        long id = 1L;
        long willChargePoint = 1_000L;
        long originPoint = 999_000L;
        long updateMillis = 100000L;
        long chargeResultPoint = originPoint + willChargePoint;

        given(userPointTable.selectById(id))
                .willReturn(new UserPoint(id, originPoint, updateMillis));

        given(userPointTable.insertOrUpdate(id, chargeResultPoint))
                .willReturn(new UserPoint(id, chargeResultPoint, updateMillis));

        // when // then
        assertThat(pointService.chargeUserPoint(id, willChargePoint, updateMillis))
                .extracting("id", "point", "updateMillis")
                .containsExactly(id, chargeResultPoint , updateMillis);

        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, times(1)).insertOrUpdate(id, chargeResultPoint);
    }

    @Test
    void 포인트_사용_시에_사용_요청값이_0원보다_작으면_예외를_빌생한다() {
        // given
        long id = 1L;
        long willUsePoint = 0L;
        long updateMillis = 100000L;

        // when // then
        assertThatThrownBy(() -> pointService.useUserPoint(id, willUsePoint, updateMillis))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("사용 요청 금액은 0원보다 커야 합니다.");

        verify(userPointTable, never()).selectById(id);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
    }

    @Test
    void 포인트_사용_시에_사용_요청값이_1원보다_커야_포인트를_저장한다() {
        // given
        long id = 1L;
        long willUsePoint = 1L;
        long originPoint = 1L;
        long updateMillis = 100000L;
        long useResultPoint = originPoint - willUsePoint;

        given(userPointTable.selectById(id))
                .willReturn(new UserPoint(id, originPoint, updateMillis));

        given(userPointTable.insertOrUpdate(id, useResultPoint))
                .willReturn(new UserPoint(id,  useResultPoint, updateMillis));

        assertThat(pointService.useUserPoint(id, willUsePoint, updateMillis))
                .extracting("id", "point", "updateMillis")
                .containsExactly(id, useResultPoint, updateMillis);

        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, times(1)).insertOrUpdate(id, useResultPoint);
    }

    @Test
    void 포인트_사용_시에_사용_요청값이_1_000_000원보다_크면_예외를_발생한다() {
        // given
        long id = 1L;
        long willUsePoint = 1_000_001L;
        long updateMillis = 100000L;

        // when // then
        assertThatThrownBy(() -> pointService.useUserPoint(id, willUsePoint, updateMillis))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("사용 요청 금액은 최대 100만원을 초과할 수 없습니다.");

        verify(userPointTable, never()).selectById(id);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
    }

    @Test
    void 포인트_사용_시에_사용_요청값이_1_000_000원_이하면_포인트_충전_성공한다() {
        // given
        long id = 1L;
        long willUsePoint = 1_000_000L;
        long originPoint = 1_000_000L;
        long updateMillis = 100000L;
        long useResultPoint = originPoint - willUsePoint;

        given(userPointTable.selectById(id))
                .willReturn(new UserPoint(id, originPoint, updateMillis));

        given(userPointTable.insertOrUpdate(id, useResultPoint))
                .willReturn(new UserPoint(id, useResultPoint, updateMillis));

        assertThat(pointService.useUserPoint(id, willUsePoint, updateMillis))
                .extracting("id", "point", "updateMillis")
                .containsExactly(id, useResultPoint, updateMillis);

        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, times(1)).insertOrUpdate(id, useResultPoint);
    }

    @Test
    void 포인트_사용_시에_포인트를_차감한_결과값이_0원보다_작으면_예외를_발생한다() {
        // given
        long id = 1L;
        long willUsePoint = 1001L;
        long originPoint = 1000L;
        long updateMillis = 100000L;

        given(userPointTable.selectById(id))
                .willReturn(new UserPoint(id, originPoint, updateMillis));

        // when // then
        assertThatThrownBy(() -> pointService.useUserPoint(id, willUsePoint, updateMillis))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("사용하려는 포인트는 보유한 포인트보다 클 수 없습니다.");

        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
    }

    @Test
    void 포인트_사용_시에_포인트를_차감한_결과값이_0원_이상이어야_포인트를_저장한다() {
        // given
        long id = 1L;
        long willUsePoint = 1000L;
        long originPoint = 1000L;
        long updateMillis = 100000L;
        long useResultPoint = originPoint - willUsePoint;

        given(userPointTable.selectById(1L))
                .willReturn(new UserPoint(id, originPoint, updateMillis));

        given(userPointTable.insertOrUpdate(id, useResultPoint))
                .willReturn(new UserPoint(id, useResultPoint, updateMillis));

        // when // then
        assertThat(pointService.useUserPoint(id, willUsePoint, updateMillis))
                .extracting("id", "point", "updateMillis")
                .containsExactly(id, useResultPoint , updateMillis);

        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, times(1)).insertOrUpdate(id, useResultPoint);
    }

    @Test
    void 포인트를_충전하면_포인트_충전_히스토리_내역이_저장된다() {
        // given
        long id = 1L;
        long willChargePoint = 1000L;
        long originPoint = 100L;
        TransactionType type = TransactionType.CHARGE;
        long updateMillis = 100000L;
        long chargeResultPoint = originPoint + willChargePoint;

        given(userPointTable.selectById(1L))
                .willReturn(new UserPoint(id, originPoint, updateMillis));

        given(userPointTable.insertOrUpdate(id, chargeResultPoint))
                .willReturn(new UserPoint(id, chargeResultPoint, updateMillis));

        given(pointHistoryTable.insert(id, willChargePoint, type, updateMillis))
                .willReturn(new PointHistory(1L, id, willChargePoint, type, updateMillis));

        pointService.chargeUserPoint(id, willChargePoint, updateMillis);

        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, times(1)).insertOrUpdate(id, chargeResultPoint);
        verify(pointHistoryTable, times(1)).insert(id, willChargePoint, type, updateMillis);
    }

    @Test
    void 포인트를_사용하면_포인트_사용_히스토리_내역이_저장된다() {
        // given
        long id = 1L;
        long willUsePoint = 1000L;
        long originPoint = 10000L;
        TransactionType type = TransactionType.USE;
        long updateMillis = 100000L;
        long useResultPoint = originPoint - willUsePoint;

        given(userPointTable.selectById(1L))
                .willReturn(new UserPoint(id, originPoint, updateMillis));

        given(userPointTable.insertOrUpdate(id, useResultPoint))
                .willReturn(new UserPoint(id, useResultPoint, updateMillis));

        given(pointHistoryTable.insert(id, willUsePoint, type, updateMillis))
                .willReturn(new PointHistory(1L, id, willUsePoint, type, updateMillis));

        pointService.useUserPoint(id, willUsePoint, updateMillis);

        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, times(1)).insertOrUpdate(id, useResultPoint);
        verify(pointHistoryTable, times(1)).insert(id, willUsePoint, type, updateMillis);
    }
}