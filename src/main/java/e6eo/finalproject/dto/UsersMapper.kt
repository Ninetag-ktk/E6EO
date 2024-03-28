package e6eo.finalproject.dto;


import e6eo.finalproject.entity.UsersEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersMapper extends JpaRepository<UsersEntity, String> {
    
    @Modifying
    @Transactional
    @Query(value = "update users u set u.observeToken=:observeToken where u.userId=:userId")
    void updateObserveByUserId(@Param("userId") String userId, @Param("observeToken") String observeToken);

    @Modifying
    @Transactional
    @Query(value = "update users u set u.observeToken=null where u.observeToken=:observeToken")
    void emptyObserve(@Param("observeToken") String observeToken);

    @Modifying
    @Transactional
    @Query(value = "update users u set u.innerId=null where u.observeToken=:observeToken")
    void emptyInnerId(@Param("observeToken") String observeToken);

    @Query(value = "select u from users u where u.innerId=:innerId")
    Optional<UsersEntity> findByInnerId(@Param("innerId") String innerId);

    @Query(value = "select u from users u where u.observeToken=:observeToken")
    Optional<UsersEntity> findByObserveToken(@Param("observeToken") String observeToken);

    @Query(value = "select u.refreshToken from users u where u.observeToken=:observeToken")
    Optional<String> getRefreshTokenByObserve(@Param("observeToken") String observeToken);

    @Modifying
    @Transactional
    @Query(value = "update users u set u.innerId=:innerId, u.refreshToken=:refreshToken where u.userId=:userId")
    void mergeWithInnerId(@Param("userId") String userId, @Param("innerId") String innerId, @Param("refreshToken") String refreshToken);

    @Modifying
    @Transactional
    @Query(value = "update users u set u.innerId=:innerId, u.refreshToken=:refreshToken, u.observeToken=:observeToken where u.userId=:userId")
    void mergeWithInnerId(@Param("userId") String userId, @Param("innerId") String innerId, @Param("refreshToken") String refreshToken, @Param("observeToken") String observeToken);

    @Modifying
    @Transactional
    @Query(value = "update users u set u.refreshToken=:refreshToken where u.userId=:userId")
    void updateRefreshToken(@Param("userId") String userId, @Param("refreshToken") String refreshToken);

    @Modifying
    @Transactional
    @Query(value = "update users u set u.pw=:pw, u.nickName=:nickName where u.userId=:userId")
    void updateUserInfoById(@Param("userId") String userId, @Param("pw") String pw, @Param("nickName") String nickName);
}
