package e6eo.finalproject.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import lombok.Data
import lombok.NoArgsConstructor

@Entity(name = "users")
@Data
@NoArgsConstructor
data class UsersEntity(
    @Id
    @Column(name = "user_id", unique = true, nullable = false)
    var userId: String? = null,

    @Column(name = "pw", nullable = true)
    var pw: String? = null,

    @Column(name = "nickname", nullable = false)
    var nickName: String? = null,

    @Column(name = "observe_token", nullable = true, unique = true)
    var observeToken: String? = null,

    @Column(name = "inner_id", nullable = true)
    var innerId: String? = null,

    @Column(name = "refresh_token", nullable = true)
    var refreshToken: String? = null
)