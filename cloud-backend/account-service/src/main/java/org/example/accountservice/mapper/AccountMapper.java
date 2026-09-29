package org.example.accountservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.accountservice.entity.Account;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}
