# 동시성 제어 방식에 대한 분석 및 보고서 작성

## 목차
### 1. 동시성 제어 방법 비교 
### 2. 채택한 동시성 제어 구현 방법
### 3. 동시성 제어 테스트

----

# 1. 동시성 제어 방법 비교
## 1. Synchronized
`synchronized` 키워드를 메서드의 선언부에 작성하여 특정 코드 블록이나 메서드에 대해 한 번에 하나의 스레드만 접근하도록 제한하여 스레드의 안정성을 보장합니다.
### 장점 
* 키워드 하나로 동기화 구현이 가능합니다.
* `synchronized` 키워드가 붙은 블록이나 메서드가 종료되면 잠금이 자동으로 해제됩니다.
### 단점 
* 하나의 프로세스 안에서만 보장되므로 서버가 여러대라면 여러 스레드에서 동시에 접속할 수 있어 레이스 컨디션 문제가 발생합니다.
* 여러 스레드가 경쟁할 경우, 대기 시간이 증가하여 성능이 저하될 수 있습니다.
> 레이스 컨디션이란 두 개 이상의 스레드가 공유 리소스에 동시 접근하여 작업을 수행할 때 발생하는 문제를 말합니다.

## 2. ConcurrentHashMap
자바에서 제공하는 스레드 안전한 해시 맵이며, 멀티스레드 환경에서도 데이터의 일관성을 보장합니다.
세그먼트나 버킷 단위로 동기화를 처리합니다.
### 장점 
* 멀티스레드 환경에서도 안전하게 동작하며, 데이터를 읽고 쓰는 작업에서 충돌이 발생하지 않습니다.
* 부분적으로 특정 버킷에만 락을 걸 수 있어, 쓰기 작업을 병렬로 처리할 수 있습니다.

### 단점 
* 동시 쓰기 작업이 많을 경우, 특정 버킷에서 락 경쟁이 발생해 성능 저하가 생길 수 있습니다.

## 3. ReentrantLock
### 장점 
* 락 획득 및 해제를 명시적으로 제어할 수 있어 복잡한 동기화 로직을 구현할 수 있습니다.
* 공정 모드를 설정하면 락 요청 순서를 보장할 수 있습니다.

### 단점 
* 락 해제를 명시하지 않으면 데드락이 발생할 수 있습니다.

--- 
# 2. 채택한 동시성 제어 구현 방법
### 동시성 요구 사항
* 서로 다른 유저에 대한 요청은 각각 동시에 실행될 수 있어야 한다.
* 같은 유저에 대한 요청은 한번에 하나만 실행될 수 있어야 한다.

동시성을 구현하기 위해 ConcurrentHashMap을 사용하여 유저별 공정 모드의 ReentrantLock을 관리하는 방식을 채택했습니다.
> ReentrantLock을 생성할 때 공정 모드를 설정하면 락 요청 순서를 보장합니다. 
> ```java
> ReentrantLock lock = new ReentrantLock(true);
> ```


### 구조:
* key: 유저의 ID.
* value: 해당 유저의 요청을 제어하는 ReentrantLock.

### 작동 원리:
* 다른 유저의 요청: 서로 다른 ReentrantLock 객체를 사용하므로 동시에 실행 가능합니다.
* 같은 유저의 요청: 동일한 ReentrantLock 객체를 공유하므로, 요청이 순차적으로 처리됩니다.

이를 통해, 요구 사항을 충족하는 유저별 독립적인 동시성 제어를 구현할 수 있습니다.<br>
아래 예시 코드는 PointService의 포인트 충전 로직입니다. 

### 예시 코드
```java
private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

public UserPoint chargeUserPoint(long id, long amount) {
    ReentrantLock lock = userLocks.computeIfAbsent(id, userId -> new ReentrantLock(true));

    lock.lock();
    try {
        if(amount <= 0) {
            throw new RuntimeException("충전 요청 금액은 0원보다 커야 합니다.");
        }
        if(amount > MAX_AVAILABLE_POINT) {
            throw new RuntimeException("충전 요청 금액은 최대 100만원을 초과할 수 없습니다.");
        }

        UserPoint originUserPoint = userPointTable.selectById(id);
        if(originUserPoint.point() + amount > MAX_AVAILABLE_POINT) {
            throw new RuntimeException("충전 후 보유 포인트는 최대 100만원을 초과할 수 없습니다.");
        }

        UserPoint chargedUserPoint = userPointTable.insertOrUpdate(id, originUserPoint.point() + amount);

        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return chargedUserPoint;
    } finally {
        lock.unlock();
    }
}
```
실행 순서 요약
1. 유저 id를 기반으로 ReentrantLock을 생성합니다. 
2. 포인트 충전 로직을 실행합니다. 
3. 포인트 충전 로직 실행 완료 후 ReentrantLock을 해제하여 동일 유저의 다음 요청을 처리합니다. 

---

# 3. 동시성 제어 테스트
## 테스트 방법
`ExecutorService`를 사용하여 멀티 스레드를 생성하고, 각 스레드에서 충전 및 사용 작업을 반복하도록 합니다.</br>
모든 충전 및 사용 작업이 완료된 후, 예상되는 연산 결과 포인트와 유저의 실제 포인트를 비교하여 일치 여부를 확인합니다.  

## 테스트 예시 코드 
```java
@Test
    void 한_명의_유저에_대해_동시에_충전과_사용_요청을_하면_정상적으로_모두_처리된다() throws InterruptedException {
        // given
        long userId = 1L;
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long chargeAmount = 100L;
        long useAmount = 10L;

        pointService.chargeUserPoint(userId, 10L);

        // when
        for(int i = 0; i < threadCount; i++) {
            int index = i;
            executorService.submit(() -> {
                try {
                    if(index % 2 != 0) {
                        pointService.chargeUserPoint(userId, chargeAmount);
                    } else {
                        pointService.useUserPoint(userId, useAmount);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        UserPoint resultUserPoint = pointService.selectUserPointById(userId);

        assertThat(resultUserPoint)
                .extracting("id", "point")
                .containsExactly(userId, 460L);
    }
```

## 결과 및 분석
멀티 스레드 생성 후 각 스레드는 홀수 인덱스에서는 포인트 충전, 짝수 인덱스에서는 포인트 사용 작업을 반복 실행했습니다.
모든 작업 완료 후 예상 포인트와 실제 포인트가 일치하여 동시성 요구사항이 충족됨을 확인했습니다.

## 개선 방안 
* 작업이 완료된 후, 더 이상 사용되지 않는 ReentrantLock을 ConcurrentHashMap에서 제거하는 코드를 추가하여 메모리 누수를 방지합니다.
* 시스템의 안정성을 더 철저히 검증하기 위해 스레드 수를 변경하며 테스트하여 다양한 동시성 환경에서도 요구사항이 충족되는지 확인해봅니다.

## 결론 
ConcurrentHashMap과 ReentrantLock을 활용한 동시성 제어 방식은 동시성 요구사항을 충족하고, 
동일 유저의 요청은 순차적으로, 서로 다른 유저의 요청은 병렬로 처리되며, 예상 결과와 실제 결과가 일치함을 확인했습니다.


