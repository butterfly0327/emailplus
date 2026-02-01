package com.backend.backend.domain.mapping.service;

import com.backend.backend.domain.mapping.dto.ActionResponse;
import com.backend.backend.global.exception.CustomException;
import com.backend.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
//@Sql(scripts = {"/sql/schema.sql", "/sql/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import({ActionService.class, com.backend.backend.domain.mapping.validator.MappingItemValidator.class})
class ActionServiceIntegrationTest {

    @Autowired
    private ActionService actionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE mapping_items");
        jdbcTemplate.execute("TRUNCATE TABLE mappings");
        jdbcTemplate.execute("TRUNCATE TABLE gestures");
        jdbcTemplate.execute("TRUNCATE TABLE actions");
        jdbcTemplate.execute("TRUNCATE TABLE accounts");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Test
    @DisplayName("DB에서 사용되지 않은 기능 목록을 정상적으로 조회한다")
    void findNotUseAction_Success() {
        jdbcTemplate.execute("INSERT INTO accounts (account_id, email, username,password) VALUES (1, 'test@example.com', 'Test User','EE')");
        jdbcTemplate.execute("INSERT INTO gestures (gesture_id,gesture_code,gesture_name) VALUES (1, 'Swipe Left','e')");
        jdbcTemplate.execute("INSERT INTO actions (action_id, action_code,action_name) VALUES (1, 'Next Slide','e'), (2, 'Previous Slide','e'), (3, 'Start Presentation','eeeeee')");
        jdbcTemplate.execute("INSERT INTO mappings (mapping_id, account_id, mapping_name) VALUES (1, 1, 'Test Mapping')");
        jdbcTemplate.execute("INSERT INTO mapping_items (mapping_id, gesture_id, action_id) VALUES (1, 1, 1)");

        List<ActionResponse> result = actionService.findNotUseAction(1);

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result).extracting("actionId").containsExactlyInAnyOrder(2L, 3L);
    }

    @Test
    @DisplayName("모든 기능이 사용 중일 때 예외를 발생시킨다")
    void findNotUseAction_AllActionsUsed() {
        jdbcTemplate.execute("INSERT INTO accounts (account_id, email, username,password) VALUES (1, 'test@example.com', 'Test User','EE')");
        jdbcTemplate.execute("INSERT INTO gestures (gesture_id, gesture_code,gesture_name) VALUES (1, 'Swipe Left','e'), (2, 'Swipe Right','e')");
        jdbcTemplate.execute("INSERT INTO actions (action_id,action_code,action_name) VALUES (1, 'Next Slide','e'), (2, 'Previous Slide','e')");
        jdbcTemplate.execute("INSERT INTO mappings (mapping_id, account_id, mapping_name) VALUES (1, 1, 'Test Mapping')");
        jdbcTemplate.execute("INSERT INTO mapping_items (mapping_id, gesture_id, action_id) VALUES (1, 1, 1), (1, 2, 2)");

        assertThatThrownBy(() -> actionService.findNotUseAction(1))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTION_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 매핑으로 조회 시 모든 기능을 반환한다")
    void findNotUseAction_MappingNotExists() {
        jdbcTemplate.execute("INSERT INTO actions (action_id, action_code,action_name) VALUES (1, 'Next Slide','e'), (2, 'Previous Slide','e')");

        List<ActionResponse> result = actionService.findNotUseAction(999);

        assertThat(result).hasSize(2);
        assertThat(result).extracting("actionId").containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("유효하지 않은 mappingId로 조회 시 예외를 발생시킨다")
    void findNotUseAction_InvalidMappingId() {
        assertThatThrownBy(() -> actionService.findNotUseAction(0))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("음수 mappingId로 조회 시 예외를 발생시킨다")
    void findNotUseAction_NegativeMappingId() {
        assertThatThrownBy(() -> actionService.findNotUseAction(-1))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("매핑 항목이 없을 때 모든 기능을 반환한다")
    void findNotUseAction_NoMappingItems() {
        jdbcTemplate.execute("INSERT INTO accounts (account_id, email, username,password) VALUES (1, 'test@example.com', 'Test User','EE')");
        jdbcTemplate.execute("INSERT INTO actions (action_id, action_code,action_name) VALUES (1, 'Next Slide','eeee'), (2, 'Previous Slide','eeee'), (3, 'Start','eeee')");
        jdbcTemplate.execute("INSERT INTO mappings (mapping_id, account_id, mapping_name) VALUES (1, 1, 'Test Mapping')");

        List<ActionResponse> result = actionService.findNotUseAction(1);

        assertThat(result).hasSize(3);
        assertThat(result).extracting("actionId").containsExactlyInAnyOrder(1L, 2L, 3L);
    }
}
