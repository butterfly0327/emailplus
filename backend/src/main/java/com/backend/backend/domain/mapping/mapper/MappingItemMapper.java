package com.backend.backend.domain.mapping.mapper;

import com.backend.backend.domain.mapping.dto.MappingDetailItemResponse;
import com.backend.backend.domain.mapping.entity.MappingItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MappingItemMapper {

    MappingItem findById(@Param("id") Long id);

    List<MappingItem> findByMappingId(@Param("mappingId") Long mappingId);

    List<MappingDetailItemResponse> findDetailByMappingId(@Param("mappingId") Long mappingId);

    int insert(MappingItem mappingItem);

    int deleteItem(@Param("itemId") int itemId);

    int deleteByMappingId(@Param("mappingId") Long mappingId);

    int changeItem(@Param("itemId") int itemId, @Param("gestureId") int gestureId, @Param("actionId") int actionId);

    boolean existsByMappingIdAndGestureId(@Param("mappingId") Long mappingId, @Param("gestureId") Long gestureId);

    boolean existsByMappingIdAndActionId(@Param("mappingId") Long mappingId, @Param("actionId") Long actionId);
}
