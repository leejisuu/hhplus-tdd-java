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

    public UserPoint selectById(long id) {
        isIdGreaterThenOne(id);

        return userPointTable.selectById(id);
    }

    public List<PointHistory> selectAllByUserId(long id) {
        isIdGreaterThenOne(id);

        return pointHistoryTable.selectAllByUserId(id);
    }

    public UserPoint saveChargeUserPoint(long id, long amount) {
        // 파라미터 유효성 체크를 한다.
        // id가 1보다 큰 양수인지
        // amount가 1보다 큰 양수인지

        // 기존 포인트를 조회하여 충전할 포인트와 합산하여 최대 포인트 100,000을 넘지 않으면 포인트를 저장한다.
        // 최대 포인트가 넘는다면 예외를 발생한다.

        // 히스토리 저장
        // PointHistoryTable에 포인트 충전 내역을 저장한다.

        // 포인트 리턴
        // 충전 완료된 총 포인트 정보를 반환한다.

        return new UserPoint(0 , 0, System.currentTimeMillis());
    }

    public UserPoint saveUseUserPoint(long id, long amount) {
        // 파라미터 유효성 체크를 한다.
        // id가 1보다 큰 양수인지
        // amount가 1보다 큰 양수인지

        // 기존 포인트를 조회하여 사용할 포인트를 차감하여 잔여 포인트가 0 이상이면 포인트를 저장한다.
        // 0 미만이라면 예외를 발생한다.

        // 히스토리 저장
        // PointHistoryTable에 포인트 사용 내역을 저장한다.

        // 포인트 리턴
        // 사용 완료된 총 포인트 정보를 반환한다.
        return new UserPoint(0 , 0, System.currentTimeMillis());
    }

    /*
    * TODO - 유효성 체크에 대한 테스트를 작성한다.
    * */
    public void isIdGreaterThenOne(long id) {
        if(id <= 0) {
            throw new IllegalArgumentException("id는 1 이상의 양수만 가능합니다.");
        }
    }
}
