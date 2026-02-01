package com.backend.backend.domain.mapping.mapper;

import com.backend.backend.domain.mapping.entity.MappingItem;
import com.backend.backend.domain.mapping.entity.MappingValidation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MappingValidationMapper {

    List<MappingItem> findByMappingId(@Param("mappingId") int mappingId);

    int countByGestureIdExcludingItem(@Param("mappingId") int mappingId, @Param("gestureId") int gestureId, @Param("excludeItemId") int excludeItemId);

    int countByActionIdExcludingItem(@Param("mappingId") int mappingId, @Param("actionId") int actionId, @Param("excludeItemId") int excludeItemId);
}
