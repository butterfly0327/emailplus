package com.backend.backend.domain.mapping.service;

import com.backend.backend.domain.mapping.dto.GestureFixResponse;
import com.backend.backend.domain.mapping.dto.GestureResponse;
import com.backend.backend.domain.mapping.mapper.GestureMapper;
import com.backend.backend.domain.mapping.validator.MappingItemValidator;
import com.backend.backend.global.exception.CustomException;
import com.backend.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GestureService {
    private final GestureMapper gestureMapper;
    private final MappingItemValidator validator;

    public List<GestureResponse> findNotUseGesture(int mappingId){
        validator.validatePositiveId(mappingId);

        List<GestureResponse> gestureList = gestureMapper.findNotUseMapping(mappingId);

        if (gestureList == null || gestureList.isEmpty()) {
            throw new CustomException(ErrorCode.GESTURE_NOT_FOUND);
        }

        return gestureList;
    }

    public List<GestureFixResponse> findAvailableGesturesForAction(int mappingId, Long actionId){
        validator.validatePositiveId(mappingId);

        List<GestureFixResponse> gestureList = gestureMapper.findAvailableGesturesForAction(mappingId, actionId);

        if (gestureList == null || gestureList.isEmpty()) {
            throw new CustomException(ErrorCode.GESTURE_NOT_FOUND);
        }

        return gestureList;
    }

}
