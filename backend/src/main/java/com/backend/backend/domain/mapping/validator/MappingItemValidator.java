package com.backend.backend.domain.mapping.validator;

import com.backend.backend.domain.mapping.entity.MappingItem;
import com.backend.backend.global.exception.CustomException;
import com.backend.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MappingItemValidator {

    public void validatePositiveId(int id) {
        if (id <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    public void validateGestureAndAction(int gestureId, int actionId) {
        if (gestureId <= 0 || actionId <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    public void validateDuplicateGesture(List<MappingItem> items) {
        Set<Long> gestureIds = items.stream()
                .map(MappingItem::getGestureId)
                .collect(Collectors.toSet());

        if (gestureIds.size() != items.size()) {
            throw new CustomException(ErrorCode.DUPLICATE_GESTURE);
        }
    }

    public void validateDuplicateAction(List<MappingItem> items) {
        Set<Long> actionIds = items.stream()
                .map(MappingItem::getActionId)
                .collect(Collectors.toSet());

        if (actionIds.size() != items.size()) {
            throw new CustomException(ErrorCode.DUPLICATE_ACTION);
        }
    }

    public void validateDuplicateMappingItem(List<MappingItem> items) {
        Set<String> combinations = items.stream()
                .map(item -> item.getGestureId() + "-" + item.getActionId())
                .collect(Collectors.toSet());

        if (combinations.size() != items.size()) {
            throw new CustomException(ErrorCode.DUPLICATE_MAPPING_ITEM);
        }
    }

    public void validateNewItemConflict(List<MappingItem> existingItems, MappingItem newItem) {
        boolean gestureExists = existingItems.stream()
                .anyMatch(item -> item.getGestureId().equals(newItem.getGestureId()));

        if (gestureExists) {
            throw new CustomException(ErrorCode.DUPLICATE_GESTURE);
        }

        boolean actionExists = existingItems.stream()
                .anyMatch(item -> item.getActionId().equals(newItem.getActionId()));

        if (actionExists) {
            throw new CustomException(ErrorCode.DUPLICATE_ACTION);
        }
    }

    public void validateGestureOnly(List<MappingItem> items) {
        Set<Long> gestureIds = items.stream()
                .map(MappingItem::getGestureId)
                .collect(Collectors.toSet());

        if (gestureIds.size() != items.size()) {
            throw new CustomException(ErrorCode.DUPLICATE_GESTURE);
        }
    }

    public void validateActionOnly(List<MappingItem> items) {
        Set<Long> actionIds = items.stream()
                .map(MappingItem::getActionId)
                .collect(Collectors.toSet());

        if (actionIds.size() != items.size()) {
            throw new CustomException(ErrorCode.DUPLICATE_ACTION);
        }
    }
}
