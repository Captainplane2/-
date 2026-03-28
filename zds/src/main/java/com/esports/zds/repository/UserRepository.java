package com.esports.zds.repository;

import java.util.Optional;
import java.util.Set;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esports.zds.entity.User;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    Optional<User> findByUsername(String username);

    /**
     * 判断用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据ID集合查询用户（分页）
     * @param ids 用户ID集合
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByIdIn(Set<Long> ids, Pageable pageable);

    /**
     * 根据ID集合和高校查询用户（分页）
     * @param ids 用户ID集合
     * @param university 高校名称
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByIdInAndUniversity(Set<Long> ids, String university, Pageable pageable);

    /**
     * 根据ID集合和昵称或用户名模糊查询用户（分页）
     * @param ids 用户ID集合
     * @param nickname 昵称关键词
     * @param username 用户名关键词
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByIdInAndNicknameContainingOrUsernameContaining(
            Set<Long> ids, String nickname, String username, Pageable pageable);

    /**
     * 根据ID集合查询用户列表
     * @param ids 用户ID集合
     * @return 用户列表
     */
    List<User> findByIdIn(Set<Long> ids);
}
