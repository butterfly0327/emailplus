package com.backend.backend.domain.mapping.service;

import com.backend.backend.domain.mapping.dto.MappingItemUpdateRequest;
import com.backend.backend.domain.mapping.mapper.MappingItemMapper;
import com.backend.backend.domain.mapping.mapper.MappingValidationMapper;
import com.backend.backend.domain.mapping.validator.MappingItemValidator;
import com.backend.backend.global.exception.CustomException;
import com.backend.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MappingItemServiceTest {

    @Mock
    private MappingItemMapper itemMapper;

    @Mock
    private MappingValidationMapper mappingValidationMapper;

//    @Mock
//    private MappingItemValidator validator;
//
//    @InjectMocks
//    private MappingItemService mappingItemService;
//
//    @Nested
//    @DisplayName("deleteMappingItem 메서드는")
//    class DeleteMappingItem {
//
//        @Test
//        @DisplayName("정상적으로 매핑 항목을 삭제한다")
//        void deleteSuccess() {
//            int itemId = 1;
//            given(itemMapper.deleteItem(itemId)).willReturn(1);
//
//            int result = mappingItemService.deleteMappingItem(itemId);
//
//            assertThat(result).isEqualTo(1);
//            verify(validator).validatePositiveId(itemId);
//            verify(itemMapper).deleteItem(itemId);
//        }
//
//        @Test
//        @DisplayName("유효하지 않은 itemId는 예외를 발생시킨다")
//        void invalidItemId() {
//            int itemId = 0;
//            willThrow(new CustomException(ErrorCode.INVALID_INPUT_VALUE))
//                    .given(validator).validatePositiveId(itemId);
//
//            assertThatThrownBy(() -> mappingItemService.deleteMappingItem(itemId))
//                    .isInstanceOf(CustomException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
//
//            verify(validator).validatePositiveId(itemId);
//        }
//
//        @Test
//        @DisplayName("존재하지 않는 항목 삭제 시도 시 예외를 발생시킨다")
//        void itemNotFound() {
//            int itemId = 999;
//            given(itemMapper.deleteItem(itemId)).willReturn(0);
//
//            assertThatThrownBy(() -> mappingItemService.deleteMappingItem(itemId))
//                    .isInstanceOf(CustomException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAPPING_ITEM_NOT_FOUND);
//
//            verify(validator).validatePositiveId(itemId);
//            verify(itemMapper).deleteItem(itemId);
//        }
//    }
//
//    @Nested
//    @DisplayName("changeMappingItem 메서드는")
//    class ChangeMappingItem {
//
//        @Test
//        @DisplayName("정상적으로 매핑 항목을 수정한다")
//        void changeSuccess() {
//            int itemId = 1;
//            MappingItemUpdateRequest request = MappingItemUpdateRequest.builder()
//                    .mappingId(1L)
//                    .gestureId(2L)
//                    .actionId(3L)
//                    .build();
//
//            given(mappingValidationMapper.countByGestureIdExcludingItem(1, 2, itemId)).willReturn(0);
//            given(mappingValidationMapper.countByActionIdExcludingItem(1, 3, itemId)).willReturn(0);
//            given(itemMapper.changeItem(itemId, 2, 3)).willReturn(1);
//
//            int result = mappingItemService.changeMappingItem(itemId, request);
//
//            assertThat(result).isEqualTo(1);
//            verify(validator).validatePositiveId(1);
//            verify(validator).validateGestureAndAction(2, 3);
//            verify(mappingValidationMapper).countByGestureIdExcludingItem(1, 2, itemId);
//            verify(mappingValidationMapper).countByActionIdExcludingItem(1, 3, itemId);
//            verify(itemMapper).changeItem(itemId, 2, 3);
//        }
//
//        @Test
//        @DisplayName("유효하지 않은 mappingId는 예외를 발생시킨다")
//        void invalidMappingId() {
//            int itemId = 1;
//            MappingItemUpdateRequest request = MappingItemUpdateRequest.builder()
//                    .mappingId(0L)
//                    .gestureId(1L)
//                    .actionId(1L)
//                    .build();
//
//            willThrow(new CustomException(ErrorCode.INVALID_INPUT_VALUE))
//                    .given(validator).validatePositiveId(0);
//
//            assertThatThrownBy(() -> mappingItemService.changeMappingItem(itemId, request))
//                    .isInstanceOf(CustomException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
//        }
//
//        @Test
//        @DisplayName("유효하지 않은 gestureId는 예외를 발생시킨다")
//        void invalidGestureId() {
//            int itemId = 1;
//            MappingItemUpdateRequest request = MappingItemUpdateRequest.builder()
//                    .mappingId(1L)
//                    .gestureId(0L)
//                    .actionId(1L)
//                    .build();
//
//            willThrow(new CustomException(ErrorCode.INVALID_INPUT_VALUE))
//                    .given(validator).validateGestureAndAction(0, 1);
//
//            assertThatThrownBy(() -> mappingItemService.changeMappingItem(itemId, request))
//                    .isInstanceOf(CustomException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
//        }
//
//        @Test
//        @DisplayName("유효하지 않은 actionId는 예외를 발생시킨다")
//        void invalidActionId() {
//            int itemId = 1;
//            MappingItemUpdateRequest request = MappingItemUpdateRequest.builder()
//                    .mappingId(1L)
//                    .gestureId(1L)
//                    .actionId(0L)
//                    .build();
//
//            willThrow(new CustomException(ErrorCode.INVALID_INPUT_VALUE))
//                    .given(validator).validateGestureAndAction(1, 0);
//
//            assertThatThrownBy(() -> mappingItemService.changeMappingItem(itemId, request))
//                    .isInstanceOf(CustomException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
//        }
//
//        @Test
//        @DisplayName("중복된 gestureId가 있으면 예외를 발생시킨다")
//        void duplicateGesture() {
//            int itemId = 1;
//            MappingItemUpdateRequest request = MappingItemUpdateRequest.builder()
//                    .mappingId(1L)
//                    .gestureId(2L)
//                    .actionId(3L)
//                    .build();
//
//            given(mappingValidationMapper.countByGestureIdExcludingItem(1, 2, itemId)).willReturn(1);
//
//            assertThatThrownBy(() -> mappingItemService.changeMappingItem(itemId, request))
//                    .isInstanceOf(CustomException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_GESTURE);
//
//            verify(validator).validatePositiveId(1);
//            verify(validator).validateGestureAndAction(2, 3);
//            verify(mappingValidationMapper).countByGestureIdExcludingItem(1, 2, itemId);
//        }
//
//        @Test
//        @DisplayName("중복된 actionId가 있으면 예외를 발생시킨다")
//        void duplicateAction() {
//            int itemId = 1;
//            MappingItemUpdateRequest request = MappingItemUpdateRequest.builder()
//                    .mappingId(1L)
//                    .gestureId(2L)
//                    .actionId(3L)
//                    .build();
//
//            given(mappingValidationMapper.countByGestureIdExcludingItem(1, 2, itemId)).willReturn(0);
//            given(mappingValidationMapper.countByActionIdExcludingItem(1, 3, itemId)).willReturn(1);
//
//            assertThatThrownBy(() -> mappingItemService.changeMappingItem(itemId, request))
//                    .isInstanceOf(CustomException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_ACTION);
//
//            verify(validator).validatePositiveId(1);
//            verify(validator).validateGestureAndAction(2, 3);
//            verify(mappingValidationMapper).countByGestureIdExcludingItem(1, 2, itemId);
//            verify(mappingValidationMapper).countByActionIdExcludingItem(1, 3, itemId);
//        }
//
//        @Test
//        @DisplayName("존재하지 않는 항목 수정 시도 시 예외를 발생시킨다")
//        void itemNotFound() {
//            int itemId = 999;
//            MappingItemUpdateRequest request = MappingItemUpdateRequest.builder()
//                    .mappingId(1L)
//                    .gestureId(2L)
//                    .actionId(3L)
//                    .build();
//
//            given(mappingValidationMapper.countByGestureIdExcludingItem(1, 2, itemId)).willReturn(0);
//            given(mappingValidationMapper.countByActionIdExcludingItem(1, 3, itemId)).willReturn(0);
//            given(itemMapper.changeItem(itemId, 2, 3)).willReturn(0);
//
//            assertThatThrownBy(() -> mappingItemService.changeMappingItem(itemId, request))
//                    .isInstanceOf(CustomException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAPPING_ITEM_NOT_FOUND);
//
//            verify(validator).validatePositiveId(1);
//            verify(validator).validateGestureAndAction(2, 3);
//            verify(itemMapper).changeItem(itemId, 2, 3);
//        }
    //}
}
