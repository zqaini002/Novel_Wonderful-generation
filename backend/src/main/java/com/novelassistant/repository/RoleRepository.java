package com.novelassistant.repository;

import com.novelassistant.entity.Role;
import com.novelassistant.entity.Role.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 角色数据访问接口
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * 根据角色名称查找角色
     * @param name 角色名称
     * @return 可能存在的角色
     */
    Optional<Role> findByName(ERole name);
}