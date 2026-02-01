package com.backend.backend.domain.mapping.mapper;

import com.backend.backend.domain.mapping.dto.MappingListResponse;
import com.backend.backend.domain.mapping.entity.Mapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MappingMapper {

    List<MappingListResponse> findUserMappingList(@Param("accountId") int accountId);

    Mapping findById(@Param("id") Long id);

    int insert(Mapping mapping);

    int update(Mapping mapping);

    int delete(@Param("id") Long id);

    int deactivateAllByAccountId(@Param("accountId") Long accountId);

    Mapping findActiveByAccountId(@Param("accountId") Long accountId);

    boolean existsByAccountIdAndName(@Param("accountId") Long accountId, @Param("name") String name);

    int updateName(@Param("id") Long id, @Param("name") String name);
}
