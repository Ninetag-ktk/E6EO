package e6eo.finalproject.dto

import e6eo.finalproject.entity.UsersEntity
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface UsersMapper : JpaRepository<UsersEntity, String> {
    @Modifying
    @Transactional
    @Query(value = "update users u set u.observeToken=:observeToken where u.userId=:userId")
    fun updateObserveByUserId(@Param("userId") userId: String?, @Param("observeToken") observeToken: String?)

    @Modifying
    @Transactional
    @Query(value = "update users u set u.observeToken=null where u.observeToken=:observeToken")
    fun emptyObserve(@Param("observeToken") observeToken: String?)

    @Modifying
    @Transactional
    @Query(value = "update users u set u.innerId=null where u.observeToken=:observeToken")
    fun emptyInnerId(@Param("observeToken") observeToken: String?)

    @Query(value = "select u from users u where u.innerId=:innerId")
    fun findByInnerId(@Param("innerId") innerId: String): UsersEntity?

    @Query(value = "select u from users u where u.observeToken=:observeToken")
    fun findByObserveToken(@Param("observeToken") observeToken: String?): UsersEntity?

    @Query(value = "select u.refreshToken from users u where u.observeToken=:observeToken")
    fun getRefreshTokenByObserve(@Param("observeToken") observeToken: String): String?

    @Modifying
    @Transactional
    @Query(value = "update users u set u.innerId=:innerId, u.refreshToken=:refreshToken where u.userId=:userId")
    fun mergeWithInnerId(
        @Param("userId") userId: String?,
        @Param("innerId") innerId: String?,
        @Param("refreshToken") refreshToken: String?
    )

    @Modifying
    @Transactional
    @Query(value = "update users u set u.innerId=:innerId, u.refreshToken=:refreshToken, u.observeToken=:observeToken where u.userId=:userId")
    fun mergeWithInnerId(
        @Param("userId") userId: String?,
        @Param("innerId") innerId: String?,
        @Param("refreshToken") refreshToken: String?,
        @Param("observeToken") observeToken: String?
    )

    @Modifying
    @Transactional
    @Query(value = "update users u set u.refreshToken=:refreshToken where u.userId=:userId")
    fun updateRefreshToken(@Param("userId") userId: String?, @Param("refreshToken") refreshToken: String?)

    @Modifying
    @Transactional
    @Query(value = "update users u set u.pw=:pw, u.nickName=:nickName where u.userId=:userId")
    fun updateUserInfoById(
        @Param("userId") userId: String?,
        @Param("pw") pw: String?,
        @Param("nickName") nickName: String?
    )
}
