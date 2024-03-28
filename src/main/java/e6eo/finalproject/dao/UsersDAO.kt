package e6eo.finalproject.dao

import e6eo.finalproject.entity.CategoryEntity
import e6eo.finalproject.entity.UsersEntity
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
class UsersDAO : GoogleAPI() {
    fun expire(id: String): ResponseEntity<*> {
        try {
            notesMapper.deleteAllByUserId(id)
            categoryMapper.deleteById(id)
            usersMapper.deleteById(id)
            return ResponseEntity.ok(true)
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.ok(false)
        }
    }

    fun login(id: String, pw: String): ResponseEntity<*> {
//        System.out.println(id);
        val result: MutableMap<String, String?> = HashMap()
        val user = usersMapper.findById(id)
        if (user.isEmpty) {
//            System.out.println("noId");
            result["code"] = "400"
            result["body"] = "가입되지 않은 아이디입니다"
            return ResponseEntity.badRequest().body<Map<String, String?>>(result)
        } else if (pw != user.get().pw) {
//            System.out.println("noPw");
            result["code"] = "400"
            result["body"] = "비밀번호가 일치하지 않습니다"
            return ResponseEntity.badRequest().body<Map<String, String?>>(result)
        } else {
//            System.out.println("ok");
            result["code"] = "200"
            result["body"] = tokenManager.setObserve(id)
            return ResponseEntity.ok<Map<String, String?>>(result)
        }
    }

    fun userJoin(users: UsersEntity): ResponseEntity<*> {
        val user = usersMapper.findById(users.userId!!)
        if (user.isEmpty) {
            usersMapper.save(users)
            val category = CategoryEntity(
                userId = users.userId!!
            )
            categoryMapper.save(category)
            categoryMapper.saveDefault(users.userId!!, users.nickName!!)
            return ResponseEntity.ok(true)
        } else {
            return ResponseEntity.ok(false)
        }
    }
}