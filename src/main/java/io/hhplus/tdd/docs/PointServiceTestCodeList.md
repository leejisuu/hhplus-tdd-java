# PointService 단위 테스트 목록
* id로_특정_유저의_포인트_정보를_조회한다
* 유저의_초기_포인트는_0원이다
* id로_특정_유저의_포인트_충전_또는_사용_내역을_조회한다
* 포인트_충전_시_충전_금액이_0원보다_작으면_예외를_발생한다
* 포인트_충전_시에_충전_요청값이_1원보다_커야_포인트를_저장한다
* 포인트_충전_시에_충전_요청값이_1_000_000원보다_크면_예외를_발생한다
* 포인트_충전_시에_충전_요청값이_1_000_000원_이하여야_포인트를_저장한다
* 포인트_충전_시에_포인트를_합산한_결과값이_1_000_000원보다_크면_예외를_발생한다
* 포인트_충전_시에_포인트를_합산한_결과값이_1_000_000원_이하여야_포인트를_저장한다
* 포인트_사용_시에_사용_요청값이_0원보다_작으면_예외를_빌생한다
* 포인트_사용_시에_사용_요청값이_1원보다_커야_포인트를_저장한다
* 포인트_사용_시에_사용_요청값이_1_000_000원보다_크면_예외를_발생한다
* 포인트_사용_시에_사용_요청값이_1_000_000원_이하면_포인트를_저장한다
* 포인트_사용_시에_포인트를_차감한_결과값이_0원보다_작으면_예외를_발생한다
* 포인트_사용_시에_포인트를_차감한_결과값이_0원_이상이어야_포인트를_저장한다
* 포인트를_충전하면_포인트_충전_히스토리_내역이_저장된다
* 포인트를_사용하면_포인트_사용_히스토리_내역이_저장된다

# PointService 통합 테스트 목록
* 한_명의_유저에_대해_동시에_충전과_사용_요청을_하면_정상적으로_모두_처리된다
* 여러명의_유저가_동시에_충전_요청을_하면_정상적으로_모두_처리된다