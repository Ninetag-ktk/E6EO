package e6eo.finalproject.dto;


import e6eo.finalproject.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersMapper extends JpaRepository<UsersEntity, String> {
    @Query(value = "select u from users u where u.innerId=:innerId")
    public List<UsersEntity> findByInnerId(String innerId);

    @Query(value = "select u from users u where u.observeToken=:observeToken")
    public Optional<UsersEntity> findByObserveToken(String observeToken);

    @Query(value = "update users u set u.observeToken=:observeToken where u.userId=:userId")
    public void updateObserveByUserId(String userId, String observeToken);

    @Query(value = "update users u set u.observeToken=null where u.observeToken=:observeToken")
    public void emptyObserve(String observeToken);
}
