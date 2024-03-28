package e6eo.finalproject.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "users")
@Data
@NoArgsConstructor
public class UsersEntity {
    @Id
    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;
    @Column(name = "pw", nullable = true)
    private String pw;
    @Column(name = "nickname", nullable = false)
    private String nickName;
    @Column(name = "observe_token", nullable = true, unique = true)
    private String observeToken;
    @Column(name = "inner_id", nullable = true)
    private String innerId;
    @Column(name = "refresh_token", nullable = true)
    private String refreshToken;

    @Builder
    public UsersEntity(String userId, String pw, String nickName, String innerId, String refreshToken) {
        this.userId = userId;
        this.pw = pw;
        this.nickName = nickName;
        this.innerId = innerId;
        this.refreshToken = refreshToken;
    }


}
