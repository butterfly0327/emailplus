package com.backend.backend.domain.mapping.service;

import com.backend.backend.domain.mapping.dto.MappingListResponse;
import com.backend.backend.domain.mapping.mapper.MappingMapper;
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
class MappingServiceTest {

    @Mock
    private MappingMapper mappingMapper;

    @Mock
    private MappingItemValidator validator;

    @InjectMocks
    private MappingService mappingService;

    @Nested
    @DisplayName("userMappingList 메서드는")
    class UserMappingList {

//        @Test
//        @DisplayName("정상적으로 사용자의 매핑 목록을 반환한다")
//        void success() {
//            int accountId = 1;
//            List<MappingListResponse> expected = Arrays.asList(
//                    MappingListResponse.builder().presetId(1L).title("Mapping 1").build(),
//                    MappingListResponse.builder().presetId(2L).title("Mapping 2").build()
//            );
//            given(mappingMapper.findUserMappingList(accountId)).willReturn(expected);
//
//            List<MappingListResponse> result = mappingService.userMappingList(accountId);
//
//            assertThat(result).hasSize(2);
//            assertThat(result).isEqualTo(expected);
//            verify(validator).validatePositiveId(accountId);
//            verify(mappingMapper).findUserMappingList(accountId);
//        }
//
//        @Test
//        @DisplayName("유효하지 않은 accountId는 예외를 발생시킨다")
//        void invalidAccountId() {
//            int accountId = 0;
//            willThrow(new CustomException(ErrorCode.INVALID_INPUT_VALUE))
//                    .given(validator).validatePositiveId(accountId);
//
//            assertThatThrownBy(() -> mappingService.userMappingList(accountId))
//                    .isInstanceOf(CustomException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
//
//            verify(validator).validatePositiveId(accountId);
//        }
//
//        @Test
//        @DisplayName("음수 accountId는 예외를 발생시킨다")
//        void negativeAccountId() {
//            int accountId = -1;
//            willThrow(new CustomException(ErrorCode.INVALID_INPUT_VALUE))
//                    .given(validator).validatePositiveId(accountId);
//
//            assertThatThrownBy(() -> mappingService.userMappingList(accountId))
//                    .isInstanceOf(CustomException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
//
//            verify(validator).validatePositiveId(accountId);
//        }
//
//        @Test
//        @DisplayName("매핑 목록이 null이면 예외를 발생시킨다")
//        void nullMappingList() {
//            int accountId = 1;
//            given(mappingMapper.findUserMappingList(accountId)).willReturn(null);
//
//            assertThatThrownBy(() -> mappingService.userMappingList(accountId))
//                    .isInstanceOf(CustomException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAPPING_NOT_FOUND);
//
//            verify(validator).validatePositiveId(accountId);
//            verify(mappingMapper).findUserMappingList(accountId);
//        }
//
//        @Test
//        @DisplayName("매핑 목록이 비어있으면 예외를 발생시킨다")
//        void emptyMappingList() {
//            int accountId = 1;
//            given(mappingMapper.findUserMappingList(accountId)).willReturn(Collections.emptyList());
//
//            assertThatThrownBy(() -> mappingService.userMappingList(accountId))
//                    .isInstanceOf(CustomException.class)
//                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAPPING_NOT_FOUND);
//
//            verify(validator).validatePositiveId(accountId);
//            verify(mappingMapper).findUserMappingList(accountId);
//        }
    }
}
