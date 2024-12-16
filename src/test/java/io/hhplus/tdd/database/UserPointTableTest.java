package io.hhplus.tdd.database;

import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserPointTableTest {

    @Autowired
    private UserPointTable userPointTable;

    @DisplayName("id로 유저의 포인트 정보를 조회한다.")
    @Test
    void selectById() {
        // given
        long id = 1;
        long point = 10;
        userPointTable.insertOrUpdate(id, point);

        // when
        UserPoint selectedUserPoint = userPointTable.selectById(id);

        // then
        assertThat(selectedUserPoint)
                .extracting("id", "point")
                .contains(id, point);
    }

    @DisplayName("")
    @Test
    void insertOrUpdate() {
    }
}