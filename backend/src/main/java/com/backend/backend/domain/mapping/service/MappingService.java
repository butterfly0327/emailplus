package com.backend.backend.domain.mapping.service;

import com.backend.backend.domain.auth.entity.Account;
import com.backend.backend.domain.mapping.dto.AddMappingItemRequest;
import com.backend.backend.domain.mapping.dto.ApplyMappingSetResponse;
import com.backend.backend.domain.mapping.dto.ApplyMappingResponse;
import com.backend.backend.domain.mapping.dto.ChangeMappingSetNameRequest;
import com.backend.backend.domain.mapping.dto.ChangeMappingSetNameResponse;
import com.backend.backend.domain.mapping.dto.MappingCreateRequest;
import com.backend.backend.domain.mapping.dto.MappingDetailItemResponse;
import com.backend.backend.domain.mapping.dto.MappingDetailResponse;
import com.backend.backend.domain.mapping.dto.MappingListResponse;
import com.backend.backend.domain.mapping.entity.Mapping;
import com.backend.backend.domain.mapping.entity.MappingItem;
import com.backend.backend.domain.mapping.mapper.ActionMapper;
import com.backend.backend.domain.mapping.mapper.GestureMapper;
import com.backend.backend.domain.mapping.mapper.MappingItemMapper;
import com.backend.backend.domain.mapping.mapper.MappingMapper;
import com.backend.backend.domain.mapping.validator.MappingItemValidator;
import com.backend.backend.global.config.JwtTokenUtil;
import com.backend.backend.global.exception.CustomException;
import com.backend.backend.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MappingService {
    private final MappingMapper mappingMapper;
    private final MappingItemMapper mappingItemMapper;
    private final GestureMapper gestureMapper;
    private final ActionMapper actionMapper;
    private final MappingItemValidator validator;
    private final JwtTokenUtil tokenUtil;

    public List<MappingListResponse> userMappingList(HttpServletRequest request){
        String accessToken = tokenUtil.resolveAccessToken(request);
        int accountId = Integer.parseInt(tokenUtil.getClaims(accessToken).getSubject());

        validator.validatePositiveId(accountId);

        List<MappingListResponse> mappingList = mappingMapper.findUserMappingList(accountId);

        if (mappingList == null || mappingList.isEmpty()) {
            throw new CustomException(ErrorCode.MAPPING_NOT_FOUND);
        }

        return mappingList;
    }


    @Transactional
    public Mapping createMapping(HttpServletRequest userRequest, MappingCreateRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new CustomException(ErrorCode.MISSING_INPUT_VALUE);
        }

        String accessToken = tokenUtil.resolveAccessToken(userRequest);
        long accountId = Long.parseLong(tokenUtil.getClaims(accessToken).getSubject());
        Account account = Account.builder().id(accountId).build();//이거 빌더로 설정하지말고 ID는 MYBATIS SQL로 설정하기
        Mapping mapping = Mapping.builder()
                .account(account)
                .name(request.getTitle())
                .isActive(false)
                .changeType(Mapping.ChangeType.CREATE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        mappingMapper.insert(mapping);

        return mapping;
    }

    @Transactional
    public MappingItem addMappingItem(Long mappingId, AddMappingItemRequest request) {
        // 매핑셋 존재 여부 확인
        Mapping mapping = mappingMapper.findById(mappingId);
        if (mapping == null) {
            throw new CustomException(ErrorCode.SET_NOT_FOUND);
        }

        // 필수 입력값 확인
        if (request.getActionId() == null || request.getGestureId() == null) {
            throw new CustomException(ErrorCode.MISSING_INPUT_VALUE);
        }

        // gesture/action 유효성 확인
        if (!gestureMapper.existsById(request.getGestureId())) {
            throw new CustomException(ErrorCode.MAPPING_INVALID);
        }
        if (!actionMapper.existsById(request.getActionId())) {
            throw new CustomException(ErrorCode.MAPPING_INVALID);
        }

        // 중복 체크 - 같은 gesture가 이미 매핑에 존재하는지
        if (mappingItemMapper.existsByMappingIdAndGestureId(mappingId, request.getGestureId())) {
            throw new CustomException(ErrorCode.DUPLICATE_GESTURE);
        }

        // 중복 체크 - 같은 action이 이미 매핑에 존재하는지
        if (mappingItemMapper.existsByMappingIdAndActionId(mappingId, request.getActionId())) {
            throw new CustomException(ErrorCode.DUPLICATE_ACTION);
        }

        MappingItem item = MappingItem.builder()
                .mapping(mapping)
                .gestureId(request.getGestureId())
                .actionId(request.getActionId())
                .build();

        mappingItemMapper.insert(item);

        return item;
    }

    @Transactional
    public Mapping activateMapping(Long mappingId) {
        Mapping mapping = mappingMapper.findById(mappingId);
        if (mapping == null) {
            throw new CustomException(ErrorCode.MAPPING_NOT_FOUND);
        }

        Long accountId = mapping.getAccount().getId();
        mappingMapper.deactivateAllByAccountId(accountId);

        Mapping activatedMapping = Mapping.builder()
                .id(mappingId)
                .account(mapping.getAccount())
                .name(mapping.getName())
                .isActive(true)
                .changeType(Mapping.ChangeType.APPLY_PRESET)
                .note(mapping.getNote())
                .createdAt(mapping.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        mappingMapper.update(activatedMapping);
        return activatedMapping;
    }

    @Transactional
    public Long deleteMapping(HttpServletRequest request, Long mappingId) {
        // 토큰에서 accountId 추출
        String accessToken = tokenUtil.resolveAccessToken(request);
        Long accountId = Long.parseLong(tokenUtil.getClaims(accessToken).getSubject());

        // 매핑셋 존재 여부 확인
        Mapping mapping = mappingMapper.findById(mappingId);
        if (mapping == null) {
            throw new CustomException(ErrorCode.MAPPING_DELETE_NOT_FOUND);
        }

        // 권한 확인 (본인 매핑셋인지)
        if (!mapping.getAccount().getId().equals(accountId)) {
            throw new CustomException(ErrorCode.DELETE_FORBIDDEN);
        }

        // 대표 매핑셋인지 확인
        if (mapping.getIsActive()) {
            throw new CustomException(ErrorCode.CANNOT_DELETE_ACTIVE_MAPPING);
        }

        mappingItemMapper.deleteByMappingId(mappingId);
        mappingMapper.delete(mappingId);

        return mappingId;
    }

    @Transactional(readOnly = true)
    public MappingDetailResponse getMappingDetail(Long mappingId) {
        Mapping mapping = mappingMapper.findById(mappingId);
        if (mapping == null) {
            throw new CustomException(ErrorCode.MAPPING_NOT_FOUND);
        }

        List<MappingDetailItemResponse> items = mappingItemMapper.findDetailByMappingId(mappingId);

        return MappingDetailResponse.builder()
                .presetId(mapping.getId())
                .title(mapping.getName())
                .items(items)
                .build();
    }

    @Transactional
    public ApplyMappingSetResponse applyMappingSet(Long presetId) {
        Mapping mapping = mappingMapper.findById(presetId);
        if (mapping == null) {
            throw new CustomException(ErrorCode.MAPPING_PRESET_NOT_FOUND);
        }

        if (mapping.getIsActive()) {
            throw new CustomException(ErrorCode.MAPPING_PRESET_ALREADY_ACTIVE);
        }

        Long accountId = mapping.getAccount().getId();
        Mapping previousMapping = mappingMapper.findActiveByAccountId(accountId);

        ApplyMappingResponse previousPreset = null;
        if (previousMapping != null) {
            previousPreset = ApplyMappingResponse.builder()
                    .mappingId(previousMapping.getId())
                    .title(previousMapping.getName())
                    .build();
        }

        mappingMapper.deactivateAllByAccountId(accountId);

        Mapping activatedMapping = Mapping.builder()
                .id(presetId)
                .account(mapping.getAccount())
                .name(mapping.getName())
                .isActive(true)
                .changeType(Mapping.ChangeType.APPLY_PRESET)
                .note(mapping.getNote())
                .createdAt(mapping.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        mappingMapper.update(activatedMapping);

        ApplyMappingResponse appliedPreset = ApplyMappingResponse.builder()
                .mappingId(activatedMapping.getId())
                .title(activatedMapping.getName())
                .build();

        return ApplyMappingSetResponse.builder()
                .appliedPreset(appliedPreset)
                .previousPreset(previousPreset)
                .appliedAt(OffsetDateTime.now())
                .build();
    }

    @Transactional
    public ChangeMappingSetNameResponse changeMappingSetName(
            HttpServletRequest request, Long mappingId, ChangeMappingSetNameRequest nameRequest) {

        // 토큰에서 accountId 추출
        String accessToken = tokenUtil.resolveAccessToken(request);
        Long accountId = Long.parseLong(tokenUtil.getClaims(accessToken).getSubject());

        // 매핑셋 존재 여부 확인
        Mapping mapping = mappingMapper.findById(mappingId);
        if (mapping == null) {
            throw new CustomException(ErrorCode.MAPPING_SET_NOT_FOUND);
        }

        // 권한 확인 (본인 매핑셋인지)
        if (!mapping.getAccount().getId().equals(accountId)) {
            throw new CustomException(ErrorCode.AUTH_FORBIDDEN);
        }

        String newTitle = nameRequest.getTitle();

        // 이름 유효성 검사 (1~30자)
        if (newTitle == null || newTitle.isBlank() || newTitle.length() > 30) {
            throw new CustomException(ErrorCode.INVALID_MAPPING_SET_NAME);
        }

        // 이름 중복 확인 (같은 유저의 다른 매핑셋과 중복되는지)
        if (!mapping.getName().equals(newTitle) && mappingMapper.existsByAccountIdAndName(accountId, newTitle)) {
            throw new CustomException(ErrorCode.MAPPING_SET_NAME_DUPLICATE);
        }

        // 이름 업데이트
        mappingMapper.updateName(mappingId, newTitle);

        return ChangeMappingSetNameResponse.builder()
                .presetId(mappingId)
                .title(newTitle)
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Transactional
    public Long deleteMappingItem(Long mappingId, Long mappingItemId) {
        // 매핑 아이템 존재 여부 확인
        MappingItem mappingItem = mappingItemMapper.findById(mappingItemId);
        if (mappingItem == null) {
            throw new CustomException(ErrorCode.MAPPING_ITEM_NOT_FOUND);
        }

        // 해당 매핑셋에 속한 아이템인지 확인 (매핑셋 존재 여부도 함께 검증)
        if (!mappingItem.getMapping().getId().equals(mappingId)) {
            throw new CustomException(ErrorCode.MAPPING_ITEM_NOT_BELONG);
        }

        mappingItemMapper.deleteItem(mappingItemId.intValue());

        return mappingItemId;
    }
}