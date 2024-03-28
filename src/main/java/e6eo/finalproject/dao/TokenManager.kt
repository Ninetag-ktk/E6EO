package e6eo.finalproject.dao;

import e6eo.finalproject.dto.UsersMapper;
import e6eo.finalproject.entity.UsersEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenManager {

    @Autowired
    private final UsersMapper usersMapper;

    private String observeGenerator() {
        // 36글자의 랜덤한 객체 생성
        return UUID.randomUUID().toString();
    }

    public String generateObserve() {
        String observe;
        do {
            observe = observeGenerator();
        } while (usersMapper.findByObserveToken(observe).isPresent());
        return observe;
    }

    public String setObserve(String userId) {
        String observe;
        Optional<UsersEntity> user = usersMapper.findById(userId);
        // user에 옵저브 토큰이 없다면 새로 값을 입력
        // user에 옵저브 토큰이 이미 있다면 해당 값을 반환
        if (user.get().getObserveToken()==null) {
            do {
                // 랜덤값 생성
                observe = observeGenerator();
                // 랜덤값을 가진 객체가 이미 있다면 반복
            } while (usersMapper.findByObserveToken(observe).isPresent());
            // 랜덤값을 가진 객체가 없다면 DB에 저장
            usersMapper.updateObserveByUserId(userId, observe);
        } else {
            observe = user.get().getObserveToken();
        }
        // 랜덤값을 return하여 바로 set할 수 있도록 함
        return observe;
    }
    public String setObserveByInnerId(String innerId) {
        String observe;
        Optional<UsersEntity> user = usersMapper.findByInnerId(innerId);
        // user에 옵저브 토큰이 없다면 새로 값을 입력
        // user에 옵저브 토큰이 이미 있다면 해당 값을 반환
        if (user.get().getObserveToken()==null) {
            do {
                // 랜덤값 생성
                observe = observeGenerator();
                // 랜덤값을 가진 객체가 이미 있다면 반복
            } while (usersMapper.findByObserveToken(observe).isPresent());
            // 랜덤값을 가진 객체가 없다면 DB에 저장
            usersMapper.updateObserveByUserId(user.get().getUserId(), observe);
        } else {
            observe = user.get().getObserveToken();
        }
        // 랜덤값을 return하여 바로 set할 수 있도록 함
        return observe;
    }

    public void emptyObserve(String observe) {
        // 옵저브 토큰의 값을 삭제
        // 자동로그인을 설정했던 브라우저에서 설정을 해제할 경우
        // 비밀번호 변경 등 모든 로그인된 세션에서 모두 로그아웃 해야할 경우 등등
        usersMapper.emptyObserve(observe);
    }

    // 옵저브 토큰으로 유저 데이터를 받아오는 메서드
    public UsersEntity getUser(String observe) {
        return usersMapper.findByObserveToken(observe).get();
    }
}