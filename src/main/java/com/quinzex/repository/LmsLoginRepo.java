package com.quinzex.repository;

import com.quinzex.entity.LmsLogin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface LmsLoginRepo extends JpaRepository<LmsLogin,String> {
    boolean existsByEmail(String email);
    Optional<LmsLogin> findByEmail(String email);
    boolean existsByRole_RoleName(String roleName);
    Page<LmsLogin> findByIsAuthorized(Boolean isAuthorized, Pageable pageable);

    @Query("""
        SELECT u FROM LmsLogin u
        WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.lastName)  LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.email)     LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<LmsLogin> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
    SELECT DISTINCT l FROM LmsLogin l
    LEFT JOIN FETCH l.role r
    LEFT JOIN FETCH r.permissions
    WHERE LOWER(l.email) = LOWER(:email)
""")
    Optional<LmsLogin> findByEmailWithRoleAndPermissions(String email);

    @Modifying
    @Transactional
    @Query("UPDATE LmsLogin u SET u.tokenVersion = u.tokenVersion + 1 WHERE u.email = :email")
    void incrementTokenVersion(@Param("email") String email);

}
