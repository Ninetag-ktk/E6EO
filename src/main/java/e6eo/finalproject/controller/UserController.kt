package e6eo.finalproject.controller

import e6eo.finalproject.dao.UsersDAO
import e6eo.finalproject.dto.UsersMapper
import e6eo.finalproject.entity.UsersEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/user")
class UserController {
    @Autowired
    private lateinit var usersDao: UsersDAO

    @Autowired
    private lateinit var usersMapper: UsersMapper

    @PostMapping("") // 사용자 닉네임 반환
    fun passUserNickName(@RequestBody observe: String): ResponseEntity<*> {
        return ResponseEntity.ok(usersMapper.findByObserveToken(observe.replace("\"", ""))!!.nickName)
    }

    @DeleteMapping("/google") // 구글 연동 해제
    fun disconnectGoogle(@RequestBody observe: String): ResponseEntity<*> {
        usersMapper.emptyInnerId(observe.replace("\"", ""))
        return ResponseEntity.ok(true)
    }

    @PostMapping("/info") // 회원정보 조회
    fun patchUserData(@RequestBody observe: String): ResponseEntity<*> {
        val users = usersMapper.findByObserveToken(observe.replace("\"", ""))
        //        System.out.println("체크" + users);
        return ResponseEntity.ok(users)
    }

    @PatchMapping("/info") // 회원정보 수정
    fun changeUserInfo(@RequestBody user: UsersEntity): ResponseEntity<*> {
        try {
            usersMapper.updateUserInfoById(user.userId, user.pw, user.nickName)
            //            usersMapper.emptyObserve(user.getObserveToken());
            return ResponseEntity.ok(true)
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.ok(false)
        }
    }

    @DeleteMapping("/info") // 회원 탈퇴
    fun expireUserInfo(@RequestBody observe: String): ResponseEntity<*> {
        val user = usersMapper.findByObserveToken(observe.replace("\"", ""))
        return if (user != null) {
            usersDao.expire(user.userId!!)
        } else {
            ResponseEntity.ok(false)
        }
    }

    @PostMapping("/login") // 로그인
    fun login(@RequestBody req: Map<String, String>): ResponseEntity<*> {
//        System.out.println(req.get("id"));
//        System.out.println(req.get("pw"));
        return usersDao.login(req["id"]!!, req["pw"]!!)
    }

    @PostMapping("/join") // 회원가입
    fun userJoin(@RequestBody users: UsersEntity): ResponseEntity<*> {
        println(users)
        return usersDao.userJoin(users)
    }

    @PostMapping("/checkToken") // 옵저브 토큰으로 계정 유효성 검사
    fun checkObserve(@RequestBody observe: String): ResponseEntity<*> {
        val user: UsersEntity? = usersMapper.findByObserveToken(observe)
        return if (user != null) ResponseEntity.ok(true) else ResponseEntity.ok(false)
    }
}



