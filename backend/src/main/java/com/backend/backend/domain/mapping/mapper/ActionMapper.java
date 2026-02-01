package com.backend.backend.domain.mapping.mapper;

import com.backend.backend.domain.mapping.dto.ActionFixResponse;
import com.backend.backend.domain.mapping.dto.ActionResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ActionMapper {

    List<ActionResponse> findNotUseMapping(@Param("mappingId") int mappingId);

    List<ActionFixResponse> findAllWithGesture(@Param("mappingId") int mappingId);

    boolean existsById(@Param("actionId") Long actionId);
}
