package com.backend.backend.domain.mapping.service;

import com.backend.backend.domain.mapping.dto.ActionResponse;
import com.backend.backend.domain.mapping.mapper.ActionMapper;
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
class ActionServiceTest {

    @Mock
    private ActionMapper actionMapper;

    @Mock
    private MappingItemValidator validator;

    @InjectMocks
    private ActionService actionService;

    @Nested
    @DisplayName("findNotUseAction 메서드는")
    class FindNotUseAction {

        @Test
        @DisplayName("정상적으로 사용되지 않은 기능 목록을 반환한다")
        void success() {
            int mappingId = 1;
            List<ActionResponse> expected = Arrays.asList(
                    ActionResponse.builder().actionId(1L).name("Action 1").build(),
                    ActionResponse.builder().actionId(2L).name("Action 2").build()
            );
            given(actionMapper.findNotUseMapping(mappingId)).willReturn(expected);

            List<ActionResponse> result = actionService.findNotUseAction(mappingId);

            assertThat(result).hasSize(2);
            assertThat(result).isEqualTo(expected);
            verify(validator).validatePositiveId(mappingId);
            verify(actionMapper).findNotUseMapping(mappingId);
        }

        @Test
        @DisplayName("유효하지 않은 mappingId는 예외를 발생시킨다")
        void invalidMappingId() {
            int mappingId = 0;
            willThrow(new CustomException(ErrorCode.INVALID_INPUT_VALUE))
                    .given(validator).validatePositiveId(mappingId);

            assertThatThrownBy(() -> actionService.findNotUseAction(mappingId))
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

            assertThatThrownBy(() -> actionService.findNotUseAction(mappingId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);

            verify(validator).validatePositiveId(mappingId);
        }

        @Test
        @DisplayName("기능 목록이 null이면 예외를 발생시킨다")
        void nullActionList() {
            int mappingId = 1;
            given(actionMapper.findNotUseMapping(mappingId)).willReturn(null);

            assertThatThrownBy(() -> actionService.findNotUseAction(mappingId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTION_NOT_FOUND);

            verify(validator).validatePositiveId(mappingId);
            verify(actionMapper).findNotUseMapping(mappingId);
        }

        @Test
        @DisplayName("기능 목록이 비어있으면 예외를 발생시킨다")
        void emptyActionList() {
            int mappingId = 1;
            given(actionMapper.findNotUseMapping(mappingId)).willReturn(Collections.emptyList());

            assertThatThrownBy(() -> actionService.findNotUseAction(mappingId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACTION_NOT_FOUND);

            verify(validator).validatePositiveId(mappingId);
            verify(actionMapper).findNotUseMapping(mappingId);
        }

        @Test
        @DisplayName("단일 기능도 정상적으로 반환한다")
        void singleAction() {
            int mappingId = 1;
            List<ActionResponse> expected = Collections.singletonList(
                    ActionResponse.builder().actionId(1L).name("Action 1").build()
            );
            given(actionMapper.findNotUseMapping(mappingId)).willReturn(expected);

            List<ActionResponse> result = actionService.findNotUseAction(mappingId);

            assertThat(result).hasSize(1);
            assertThat(result).isEqualTo(expected);
            verify(validator).validatePositiveId(mappingId);
            verify(actionMapper).findNotUseMapping(mappingId);
        }
    }
}
