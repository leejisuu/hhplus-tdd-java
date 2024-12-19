package io.hhplus.tdd.point;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointServiceIntegrationTest {

    @Autowired
    private PointService pointService;

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

    @Test
    void 여러명의_유저가_동시에_충전_요청을_하면_정상적으로_모두_처리된다() throws InterruptedException {
        // given
        int userCount = 10;
        int startUserId = 2;
        int endUserId = userCount + 1;

        ExecutorService executorService = Executors.newFixedThreadPool(userCount);
        CountDownLatch latch = new CountDownLatch(userCount);

        long chargeAmount = 1000L;

        // when
        for(int i = startUserId; i <= endUserId; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    pointService.chargeUserPoint(userId, chargeAmount);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        for(int i = startUserId; i <= endUserId; i++) {
            assertThat(pointService.selectUserPointById(i))
                    .extracting("id", "point")
                    .containsExactly((long)i, chargeAmount);
        }
    }
}
