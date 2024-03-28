package e6eo.finalproject.dao;

import e6eo.finalproject.entity.CategoryEntity;
import e6eo.finalproject.entity.UsersEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UsersDAO extends GoogleAPI {

    public ResponseEntity<?> expire(String id) {
        try {
            notesMapper.deleteAllByUserId(id);
            categoryMapper.deleteById(id);
            usersMapper.deleteById(id);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
    }

    public ResponseEntity<?> login(String id, String pw) {
//        System.out.println(id);
        Map<String, String> result = new HashMap<>();
        Optional<UsersEntity> user = usersMapper.findById(id);
        if (user.isEmpty()) {
//            System.out.println("noId");
            result.put("code", "400");
            result.put("body", "가입되지 않은 아이디입니다");
            return ResponseEntity.badRequest().body(result);
        } else if (!pw.equals(user.get().getPw())) {
//            System.out.println("noPw");
            result.put("code", "400");
            result.put("body", "비밀번호가 일치하지 않습니다");
            return ResponseEntity.badRequest().body(result);
        } else {
//            System.out.println("ok");
            result.put("code", "200");
            result.put("body", tokenManager.setObserve(id));
            return ResponseEntity.ok(result);
        }
    }

    public ResponseEntity<?> userJoin(UsersEntity users) {
        Optional<UsersEntity> user = usersMapper.findById(users.getUserId());
        if (user.isEmpty()) {
            usersMapper.save(users);
            CategoryEntity category = CategoryEntity.builder().userId(users.getUserId()).build();
            categoryMapper.save(category);
            categoryMapper.insertDefault(users.getUserId(), users.getNickName());
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok(false);
        }
    }
}