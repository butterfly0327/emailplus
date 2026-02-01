package com.backend.backend.domain.mapping.service;

import com.backend.backend.domain.mapping.dto.MappingItemUpdateRequest;
import com.backend.backend.domain.mapping.mapper.MappingItemMapper;
import com.backend.backend.domain.mapping.mapper.MappingValidationMapper;
import com.backend.backend.domain.mapping.validator.MappingItemValidator;
import com.backend.backend.global.exception.CustomException;
import com.backend.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MappingItemService {
    private final MappingItemMapper itemMapper;
    private final MappingValidationMapper mappingValidationMapper;
    private final MappingItemValidator validator;

    public int deleteMappingItem(int itemId){
        validator.validatePositiveId(itemId);

        int deletedCount = itemMapper.deleteItem(itemId);

        if (deletedCount == 0) {
            throw new CustomException(ErrorCode.MAPPING_ITEM_NOT_FOUND);
        }

        return deletedCount;
    }

    public int changeMappingItem(MappingItemUpdateRequest request){
        int itemId = request.getItemId().intValue();
        int mappingId = request.getMappingId().intValue();
        int gestureId = request.getGestureId().intValue();
        int actionId = request.getActionId().intValue();

        validator.validatePositiveId(mappingId);
        validator.validateGestureAndAction(gestureId, actionId);

        int gestureCount = mappingValidationMapper.countByGestureIdExcludingItem(mappingId, gestureId, itemId);
        if (gestureCount > 0) {
            throw new CustomException(ErrorCode.DUPLICATE_GESTURE);
        }

        int actionCount = mappingValidationMapper.countByActionIdExcludingItem(mappingId, actionId, itemId);
        if (actionCount > 0) {
            throw new CustomException(ErrorCode.DUPLICATE_ACTION);
        }

        int updatedCount = itemMapper.changeItem(itemId, gestureId, actionId);

        if (updatedCount == 0) {
            throw new CustomException(ErrorCode.MAPPING_ITEM_NOT_FOUND);
        }

        return updatedCount;
    }
}
