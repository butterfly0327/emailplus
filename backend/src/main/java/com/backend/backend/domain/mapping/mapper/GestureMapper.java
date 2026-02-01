package com.backend.backend.domain.mapping.mapper;

import com.backend.backend.domain.mapping.dto.GestureFixResponse;
import com.backend.backend.domain.mapping.dto.GestureResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GestureMapper {

    List<GestureResponse> findNotUseMapping(@Param("mappingId")int mappingId);

    List<GestureFixResponse> findAvailableGesturesForAction(@Param("mappingId") int mappingId, @Param("actionId") Long actionId);

    boolean existsById(@Param("gestureId") Long gestureId);
}
