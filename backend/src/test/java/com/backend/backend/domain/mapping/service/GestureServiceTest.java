package com.backend.backend.domain.mapping.service;

import com.backend.backend.domain.mapping.dto.GestureResponse;
import com.backend.backend.domain.mapping.mapper.GestureMapper;
import com.backend.backend.domain.mapping.validator.MappingItemValidator;
import com.backend.backend.global.exception.CustomException;
import com.backend.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GestureServiceTest {

    @Mock
    private GestureMapper gestureMapper;

    @Mock
    private MappingItemValidator validator;

    @InjectMocks
    private GestureService gestureService;

    @Nested
    @DisplayName("findNotUseGesture 메서드는")
    class FindNotUseGesture {

        @Test
        @DisplayName("정상적으로 사용되지 않은 제스처 목록을 반환한다")
        void success() {
            int mappingId = 1;
            List<GestureResponse> expected = Arrays.asList(
                    GestureResponse.builder().gestureId(1L).name("Gesture 1").build(),
                    GestureResponse.builder().gestureId(2L).name("Gesture 2").build()
            );
            given(gestureMapper.findNotUseMapping(mappingId)).willReturn(expected);

            List<GestureResponse> result = gestureService.findNotUseGesture(mappingId);

            assertThat(result).hasSize(2);
            assertThat(result).isEqualTo(expected);
            verify(validator).validatePositiveId(mappingId);
            verify(gestureMapper).findNotUseMapping(mappingId);
        }

        @Test
        @DisplayName("유효하지 않은 mappingId는 예외를 발생시킨다")
        void invalidMappingId() {
            int mappingId = 0;
            willThrow(new CustomException(ErrorCode.INVALID_INPUT_VALUE))
                    .given(validator).validatePositiveId(mappingId);

            assertThatThrownBy(() -> gestureService.findNotUseGesture(mappingId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);

            verify(validator).validatePositiveId(mappingId);
        }

        @Test
        @DisplayName("음수 mappingId는 예외를 발생시킨다")
        void negativeMappingId() {
            int mappingId = -1;
            willThrow(new CustomException(ErrorCode.INVALID_INPUT_VALUE))
                    .given(validator).validatePositiveId(mappingId);

            assertThatThrownBy(() -> gestureService.findNotUseGesture(mappingId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);

            verify(validator).validatePositiveId(mappingId);
        }

        @Test
        @DisplayName("제스처 목록이 null이면 예외를 발생시킨다")
        void nullGestureList() {
            int mappingId = 1;
            given(gestureMapper.findNotUseMapping(mappingId)).willReturn(null);

            assertThatThrownBy(() -> gestureService.findNotUseGesture(mappingId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GESTURE_NOT_FOUND);

            verify(validator).validatePositiveId(mappingId);
            verify(gestureMapper).findNotUseMapping(mappingId);
        }

        @Test
        @DisplayName("제스처 목록이 비어있으면 예외를 발생시킨다")
        void emptyGestureList() {
            int mappingId = 1;
            given(gestureMapper.findNotUseMapping(mappingId)).willReturn(Collections.emptyList());

            assertThatThrownBy(() -> gestureService.findNotUseGesture(mappingId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GESTURE_NOT_FOUND);

            verify(validator).validatePositiveId(mappingId);
            verify(gestureMapper).findNotUseMapping(mappingId);
        }

        @Test
        @DisplayName("단일 제스처도 정상적으로 반환한다")
        void singleGesture() {
            int mappingId = 1;
            List<GestureResponse> expected = Collections.singletonList(
                    GestureResponse.builder().gestureId(1L).name("Gesture 1").build()
            );
            given(gestureMapper.findNotUseMapping(mappingId)).willReturn(expected);

            List<GestureResponse> result = gestureService.findNotUseGesture(mappingId);

            assertThat(result).hasSize(1);
            assertThat(result).isEqualTo(expected);
            verify(validator).validatePositiveId(mappingId);
            verify(gestureMapper).findNotUseMapping(mappingId);
        }
    }
}
