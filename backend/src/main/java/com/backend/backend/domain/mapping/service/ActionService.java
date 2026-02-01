package com.backend.backend.domain.mapping.service;

import com.backend.backend.domain.mapping.dto.ActionFixResponse;
import com.backend.backend.domain.mapping.dto.ActionResponse;
import com.backend.backend.domain.mapping.mapper.ActionMapper;
import com.backend.backend.domain.mapping.validator.MappingItemValidator;
import com.backend.backend.global.exception.CustomException;
import com.backend.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActionService {
    private final ActionMapper actionMapper;
    private final MappingItemValidator validator;

    public List<ActionResponse> findNotUseAction(int mappingId){
        validator.validatePositiveId(mappingId);

        List<ActionResponse> actionList = actionMapper.findNotUseMapping(mappingId);

        if (actionList == null || actionList.isEmpty()) {
            throw new CustomException(ErrorCode.ACTION_NOT_FOUND);
        }

        return actionList;
    }

    public List<ActionFixResponse> findAllActionsWithGesture(int mappingId){
        validator.validatePositiveId(mappingId);

        List<ActionFixResponse> actionList = actionMapper.findAllWithGesture(mappingId);

        if (actionList == null || actionList.isEmpty()) {
            throw new CustomException(ErrorCode.ACTION_NOT_FOUND);
        }

        return actionList;
    }
}
