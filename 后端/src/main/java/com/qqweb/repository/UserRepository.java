package com.qqweb.repository;

import com.qqweb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    /**
     * 搜索用户（按用户名或昵称模糊匹配）
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.nickname LIKE %:keyword%")
    List<User> searchByKeyword(String keyword);

    /**
     * 获取所有启用状态的用户（联系人列表）
     */
    List<User> findByStatus(User.Status status);
}
