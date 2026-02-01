package com.backend.backend.domain.mapping.service;

import com.backend.backend.domain.mapping.dto.MappingItemUpdateRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
//@Sql(scripts = {"/sql/schema.sql", "/sql/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import({MappingItemService.class, com.backend.backend.domain.mapping.validator.MappingItemValidator.class})
class MappingItemServiceIntegrationTest {

//    @Autowired
//    private MappingItemService mappingItemService;
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    @BeforeEach
//    void setUp() {
//        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
//        jdbcTemplate.execute("TRUNCATE TABLE mapping_items");
//        jdbcTemplate.execute("TRUNCATE TABLE mappings");
//        jdbcTemplate.execute("TRUNCATE TABLE gestures");
//        jdbcTemplate.execute("TRUNCATE TABLE actions");
//        jdbcTemplate.execute("TRUNCATE TABLE accounts");
//        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
//    }
//
//    @Test
//    @DisplayName("DB에서 매핑 항목을 정상적으로 삭제한다")
//    void deleteMappingItem_Success() {
//        jdbcTemplate.execute("INSERT INTO accounts (account_id, email, username,password) VALUES (1, 'test@example.com', 'Test User','EE')");
//        jdbcTemplate.execute("INSERT INTO gestures (gesture_id,gesture_code,gesture_name) VALUES (1, 'Swipe Left','e')");
//        jdbcTemplate.execute("INSERT INTO actions (action_id, action_code,action_name) VALUES (1, 'Next Slide','e'), (2, 'Previous Slide','e'), (3, 'Start Presentation','eeeeee')");
//        jdbcTemplate.execute("INSERT INTO mappings (mapping_id, account_id, mapping_name) VALUES (1, 1, 'Test Mapping')");
//        jdbcTemplate.execute("INSERT INTO mapping_items (mapping_id, gesture_id, action_id) VALUES (1, 1, 1)");
//        int result = mappingItemService.deleteMappingItem(1);
//
//        assertThat(result).isEqualTo(1);
//
//        Integer count = jdbcTemplate.queryForObject(
//                "SELECT COUNT(*) FROM mapping_items WHERE mapping_item_id = 1",
//                Integer.class
//        );
//        assertThat(count).isZero();
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 매핑 항목 삭제 시 예외를 발생시킨다")
//    void deleteMappingItem_NotFound() {
//        assertThatThrownBy(() -> mappingItemService.deleteMappingItem(999))
//                .isInstanceOf(CustomException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAPPING_ITEM_NOT_FOUND);
//    }
//
//    @Test
//    @DisplayName("유효하지 않은 itemId로 삭제 시도 시 예외를 발생시킨다")
//    void deleteMappingItem_InvalidId() {
//        assertThatThrownBy(() -> mappingItemService.deleteMappingItem(0))
//                .isInstanceOf(CustomException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
//    }
//
//    @Test
//    @DisplayName("DB에서 매핑 항목을 정상적으로 수정한다")
//    void changeMappingItem_Success() {
//        jdbcTemplate.execute("INSERT INTO accounts (account_id, email, username,password) VALUES (1, 'test@example.com', 'Test User','EE')");
//        jdbcTemplate.execute("INSERT INTO gestures (gesture_id,gesture_code,gesture_name) VALUES (1, 'Swipe Left','e')");
//        jdbcTemplate.execute("INSERT INTO actions (action_id, action_code,action_name) VALUES (1, 'Next Slide','e'), (2, 'Previous Slide','e'), (3, 'Start Presentation','eeeeee')");
//        jdbcTemplate.execute("INSERT INTO mappings (mapping_id, account_id, mapping_name) VALUES (1, 1, 'Test Mapping')");
//        jdbcTemplate.execute("INSERT INTO mapping_items (mapping_id, gesture_id, action_id) VALUES (1, 1, 1)");
//        MappingItemUpdateRequest request = MappingItemUpdateRequest.builder()
//                .mappingId(1L)
//                .gestureId(2L)
//                .actionId(2L)
//                .build();
//
//        int result = mappingItemService.changeMappingItem(1, request);
//
//        assertThat(result).isEqualTo(1);
//
//        Long gestureId = jdbcTemplate.queryForObject(
//                "SELECT gesture_id FROM mapping_items WHERE mapping_item_id = 1",
//                Long.class
//        );
//        Long actionId = jdbcTemplate.queryForObject(
//                "SELECT action_id FROM mapping_items WHERE mapping_item_id = 1",
//                Long.class
//        );
//
//        assertThat(gestureId).isEqualTo(2L);
//        assertThat(actionId).isEqualTo(2L);
//    }
//
//    @Test
//    @DisplayName("중복된 gestureId로 수정 시도 시 예외를 발생시킨다")
//    void changeMappingItem_DuplicateGesture() {
//        jdbcTemplate.execute("INSERT INTO accounts (account_id, email, username,password) VALUES (1, 'test@example.com', 'Test User','EE')");
//        jdbcTemplate.execute("INSERT INTO gestures (gesture_id,gesture_code,gesture_name) VALUES (1, 'Swipe Left','e')");
//        jdbcTemplate.execute("INSERT INTO actions (action_id, action_code,action_name) VALUES (1, 'Next Slide','e'), (2, 'Previous Slide','e'), (3, 'Start Presentation','eeeeee')");
//        jdbcTemplate.execute("INSERT INTO mappings (mapping_id, account_id, mapping_name) VALUES (1, 1, 'Test Mapping')");
//        jdbcTemplate.execute("INSERT INTO mapping_items (mapping_id, gesture_id, action_id) VALUES (1, 1, 1)");
//
//        MappingItemUpdateRequest request = MappingItemUpdateRequest.builder()
//                .mappingId(1L)
//                .gestureId(2L)
//                .actionId(1L)
//                .build();
//
//        assertThatThrownBy(() -> mappingItemService.changeMappingItem(1, request))
//                .isInstanceOf(CustomException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_GESTURE);
//    }
//
//    @Test
//    @DisplayName("중복된 actionId로 수정 시도 시 예외를 발생시킨다")
//    void changeMappingItem_DuplicateAction() {
//        jdbcTemplate.execute("INSERT INTO accounts (account_id, email, username,password) VALUES (1, 'test@example.com', 'Test User','EE')");
//        jdbcTemplate.execute("INSERT INTO gestures (gesture_id,gesture_code,gesture_name) VALUES (1, 'Swipe Left','e')");
//        jdbcTemplate.execute("INSERT INTO actions (action_id, action_code,action_name) VALUES (1, 'Next Slide','e'), (2, 'Previous Slide','e'), (3, 'Start Presentation','eeeeee')");
//        jdbcTemplate.execute("INSERT INTO mappings (mapping_id, account_id, mapping_name) VALUES (1, 1, 'Test Mapping')");
//        jdbcTemplate.execute("INSERT INTO mapping_items (mapping_id, gesture_id, action_id) VALUES (1, 1, 1)");
//        MappingItemUpdateRequest request = MappingItemUpdateRequest.builder()
//                .mappingId(1L)
//                .gestureId(1L)
//                .actionId(2L)
//                .build();
//
//        assertThatThrownBy(() -> mappingItemService.changeMappingItem(1, request))
//                .isInstanceOf(CustomException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_ACTION);
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 매핑 항목 수정 시도 시 예외를 발생시킨다")
//    void changeMappingItem_NotFound() {
//        jdbcTemplate.execute("INSERT INTO accounts (account_id, email, username,password) VALUES (1, 'test@example.com', 'Test User','EE')");
//        jdbcTemplate.execute("INSERT INTO gestures (gesture_id,gesture_code,gesture_name) VALUES (1, 'Swipe Left','e')");
//        jdbcTemplate.execute("INSERT INTO actions (action_id, action_code,action_name) VALUES (1, 'Next Slide','e'), (2, 'Previous Slide','e'), (3, 'Start Presentation','eeeeee')");
//        jdbcTemplate.execute("INSERT INTO mappings (mapping_id, account_id, mapping_name) VALUES (1, 1, 'Test Mapping')");
//        jdbcTemplate.execute("INSERT INTO mapping_items (mapping_id, gesture_id, action_id) VALUES (1, 1, 1)");
//        MappingItemUpdateRequest request = MappingItemUpdateRequest.builder()
//                .mappingId(1L)
//                .gestureId(1L)
//                .actionId(1L)
//                .build();
//
//        assertThatThrownBy(() -> mappingItemService.changeMappingItem(999, request))
//                .isInstanceOf(CustomException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAPPING_ITEM_NOT_FOUND);
//    }
}
