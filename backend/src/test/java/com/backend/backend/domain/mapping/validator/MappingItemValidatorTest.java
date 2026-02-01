package com.backend.backend.domain.mapping.validator;

import com.backend.backend.domain.mapping.entity.MappingItem;
import com.backend.backend.global.exception.CustomException;
import com.backend.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MappingItemValidatorTest {

    private MappingItemValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MappingItemValidator();
    }

    @Nested
    @DisplayName("validatePositiveId 메서드는")
    class ValidatePositiveId {

        @Test
        @DisplayName("양수 ID는 통과한다")
        void validPositiveId() {
            assertThatCode(() -> validator.validatePositiveId(1))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("0은 예외를 발생시킨다")
        void invalidZeroId() {
            assertThatThrownBy(() -> validator.validatePositiveId(0))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }

        @Test
        @DisplayName("음수는 예외를 발생시킨다")
        void invalidNegativeId() {
            assertThatThrownBy(() -> validator.validatePositiveId(-1))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Nested
    @DisplayName("validateGestureAndAction 메서드는")
    class ValidateGestureAndAction {

        @Test
        @DisplayName("양수 gestureId와 actionId는 통과한다")
        void validIds() {
            assertThatCode(() -> validator.validateGestureAndAction(1, 1))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("gestureId가 0이면 예외를 발생시킨다")
        void invalidGestureIdZero() {
            assertThatThrownBy(() -> validator.validateGestureAndAction(0, 1))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }

        @Test
        @DisplayName("actionId가 0이면 예외를 발생시킨다")
        void invalidActionIdZero() {
            assertThatThrownBy(() -> validator.validateGestureAndAction(1, 0))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }

        @Test
        @DisplayName("gestureId가 음수면 예외를 발생시킨다")
        void invalidGestureIdNegative() {
            assertThatThrownBy(() -> validator.validateGestureAndAction(-1, 1))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }

        @Test
        @DisplayName("actionId가 음수면 예외를 발생시킨다")
        void invalidActionIdNegative() {
            assertThatThrownBy(() -> validator.validateGestureAndAction(1, -1))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }

        @Test
        @DisplayName("둘 다 0이면 예외를 발생시킨다")
        void bothZero() {
            assertThatThrownBy(() -> validator.validateGestureAndAction(0, 0))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Nested
    @DisplayName("validateDuplicateGesture 메서드는")
    class ValidateDuplicateGesture {

        @Test
        @DisplayName("중복되지 않은 gestureId는 통과한다")
        void noDuplicateGestures() {
            List<MappingItem> items = Arrays.asList(
                    createMappingItem(1L, 1L, 1L),
                    createMappingItem(2L, 2L, 2L),
                    createMappingItem(3L, 3L, 3L)
            );

            assertThatCode(() -> validator.validateDuplicateGesture(items))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("중복된 gestureId가 있으면 예외를 발생시킨다")
        void duplicateGestures() {
            List<MappingItem> items = Arrays.asList(
                    createMappingItem(1L, 1L, 1L),
                    createMappingItem(2L, 1L, 2L),
                    createMappingItem(3L, 3L, 3L)
            );

            assertThatThrownBy(() -> validator.validateDuplicateGesture(items))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_GESTURE);
        }
    }

    @Nested
    @DisplayName("validateDuplicateAction 메서드는")
    class ValidateDuplicateAction {

        @Test
        @DisplayName("중복되지 않은 actionId는 통과한다")
        void noDuplicateActions() {
            List<MappingItem> items = Arrays.asList(
                    createMappingItem(1L, 1L, 1L),
                    createMappingItem(2L, 2L, 2L),
                    createMappingItem(3L, 3L, 3L)
            );

            assertThatCode(() -> validator.validateDuplicateAction(items))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("중복된 actionId가 있으면 예외를 발생시킨다")
        void duplicateActions() {
            List<MappingItem> items = Arrays.asList(
                    createMappingItem(1L, 1L, 1L),
                    createMappingItem(2L, 2L, 1L),
                    createMappingItem(3L, 3L, 3L)
            );

            assertThatThrownBy(() -> validator.validateDuplicateAction(items))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_ACTION);
        }
    }

    @Nested
    @DisplayName("validateDuplicateMappingItem 메서드는")
    class ValidateDuplicateMappingItem {

        @Test
        @DisplayName("중복되지 않은 조합은 통과한다")
        void noDuplicateCombinations() {
            List<MappingItem> items = Arrays.asList(
                    createMappingItem(1L, 1L, 1L),
                    createMappingItem(2L, 2L, 2L),
                    createMappingItem(3L, 3L, 3L)
            );

            assertThatCode(() -> validator.validateDuplicateMappingItem(items))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("동일한 gestureId-actionId 조합이 있으면 예외를 발생시킨다")
        void duplicateCombinations() {
            List<MappingItem> items = Arrays.asList(
                    createMappingItem(1L, 1L, 1L),
                    createMappingItem(2L, 1L, 1L),
                    createMappingItem(3L, 3L, 3L)
            );

            assertThatThrownBy(() -> validator.validateDuplicateMappingItem(items))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_MAPPING_ITEM);
        }
    }

    @Nested
    @DisplayName("validateNewItemConflict 메서드는")
    class ValidateNewItemConflict {

        @Test
        @DisplayName("충돌하지 않는 항목은 통과한다")
        void noConflict() {
            List<MappingItem> existingItems = Arrays.asList(
                    createMappingItem(1L, 1L, 1L),
                    createMappingItem(2L, 2L, 2L)
            );
            MappingItem newItem = createMappingItem(3L, 3L, 3L);

            assertThatCode(() -> validator.validateNewItemConflict(existingItems, newItem))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("gestureId가 중복되면 예외를 발생시킨다")
        void gestureConflict() {
            List<MappingItem> existingItems = Arrays.asList(
                    createMappingItem(1L, 1L, 1L),
                    createMappingItem(2L, 2L, 2L)
            );
            MappingItem newItem = createMappingItem(3L, 1L, 3L);

            assertThatThrownBy(() -> validator.validateNewItemConflict(existingItems, newItem))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_GESTURE);
        }

        @Test
        @DisplayName("actionId가 중복되면 예외를 발생시킨다")
        void actionConflict() {
            List<MappingItem> existingItems = Arrays.asList(
                    createMappingItem(1L, 1L, 1L),
                    createMappingItem(2L, 2L, 2L)
            );
            MappingItem newItem = createMappingItem(3L, 3L, 1L);

            assertThatThrownBy(() -> validator.validateNewItemConflict(existingItems, newItem))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_ACTION);
        }
    }

    @Nested
    @DisplayName("validateGestureOnly 메서드는")
    class ValidateGestureOnly {

        @Test
        @DisplayName("중복되지 않은 gestureId는 통과한다")
        void noDuplicateGestures() {
            List<MappingItem> items = Arrays.asList(
                    createMappingItem(1L, 1L, 1L),
                    createMappingItem(2L, 2L, 1L),
                    createMappingItem(3L, 3L, 1L)
            );

            assertThatCode(() -> validator.validateGestureOnly(items))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("중복된 gestureId가 있으면 예외를 발생시킨다")
        void duplicateGestures() {
            List<MappingItem> items = Arrays.asList(
                    createMappingItem(1L, 1L, 1L),
                    createMappingItem(2L, 1L, 2L)
            );

            assertThatThrownBy(() -> validator.validateGestureOnly(items))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_GESTURE);
        }
    }

    @Nested
    @DisplayName("validateActionOnly 메서드는")
    class ValidateActionOnly {

        @Test
        @DisplayName("중복되지 않은 actionId는 통과한다")
        void noDuplicateActions() {
            List<MappingItem> items = Arrays.asList(
                    createMappingItem(1L, 1L, 1L),
                    createMappingItem(2L, 1L, 2L),
                    createMappingItem(3L, 1L, 3L)
            );

            assertThatCode(() -> validator.validateActionOnly(items))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("중복된 actionId가 있으면 예외를 발생시킨다")
        void duplicateActions() {
            List<MappingItem> items = Arrays.asList(
                    createMappingItem(1L, 1L, 1L),
                    createMappingItem(2L, 2L, 1L)
            );

            assertThatThrownBy(() -> validator.validateActionOnly(items))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_ACTION);
        }
    }

    private MappingItem createMappingItem(Long itemId, Long gestureId, Long actionId) {
        return MappingItem.builder()
                .id(itemId)
                .gestureId(gestureId)
                .actionId(actionId)
                .build();
    }
}
