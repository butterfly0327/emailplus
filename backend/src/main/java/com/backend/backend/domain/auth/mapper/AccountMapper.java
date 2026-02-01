package com.backend.backend.domain.auth.mapper;

import com.backend.backend.domain.auth.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccountMapper {

    List<Account> findAll();
    Account findById(@Param("id") Long id);
    Account findByEmail(@Param("email") String email);
    int insert(Account account);
    int update(Account account);
    int delete(@Param("id") Long id);
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    int hardDeleteByEmail(@Param("email") String email);
    int hardDeleteById(@Param("id") Long id);
}
