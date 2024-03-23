package e6eo.finalproject.controller;

import e6eo.finalproject.dao.CategoryDAO;
import e6eo.finalproject.dao.GoogleAPI;
import e6eo.finalproject.dao.NotesDAO;
import e6eo.finalproject.dao.UsersDAO;
import e6eo.finalproject.dto.CategoryMapper;
import e6eo.finalproject.dto.NotesMapper;
import e6eo.finalproject.dto.UsersMapper;
import e6eo.finalproject.entity.UsersEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/google")
@CrossOrigin(origins = "http://localhost:3000")
public class GoogleController {

    @Autowired
    private GoogleAPI googleAPI;
    @Autowired
    private UsersDAO usersDAO;
    @Autowired
    private CategoryDAO categoryDAO;
    @Autowired
    private NotesDAO notesDAO;
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private NotesMapper notesMapper;

    @GetMapping("/login")
    // 프론트에서 리다이렉트 될 수 있게끔, ResponseEntity<?> 에 URL을 String 타입으로 담아 반환
    public ResponseEntity<?> googleOAuth() throws Exception {
        Map<String, String> googleUrl = new HashMap<>();
        googleUrl.put("redirect", googleAPI.getGoogleAuthUrl());
        return ResponseEntity.ok(googleUrl);
    }

    @GetMapping("/check")
    // /google/login 에서 리다이렉트 되는 경로
    // 파라미터를 추가해 프론트 url 로 전송
    public ResponseEntity<Void> googleCheck(@RequestParam(value = "code") String authCode) throws Exception {
        googleAPI.getGoogleToken(authCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("http://localhost:3000/check?autologin=true&observe=" + googleAPI.checkGoogleEmail()))
                .build();
    }

    @PatchMapping("/patch")
    // 사후 구글 연동
    public ResponseEntity<?> googleAccountMerge(@RequestBody Map<String, String> request) {
        googleAPI.mergeGoogleAccount(request);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/updateCheck")
    public void checkGoogleAccount(@RequestBody String observe) {
        // System.out.println("구글 데이터 업데이트");
        String observeToken = observe.replace("\"", "");
        UsersEntity user = usersMapper.findByObserveToken(observeToken).get();
        System.out.println(user);
        String accessToken = null;
        if (user.getInnerId() != null) {
            accessToken = googleAPI.getNewAccessTokenByObserve(observeToken);
            if (categoryDAO.checkGoogleCategory(user, accessToken)) {
                // 구글 데이터를 받아온 적이 없는 경우
                // 데이터를 요청해서 받아옴
                notesDAO.getGoogleNotes(user, accessToken);
            } else {
                notesDAO.checkGoogleNotes(user, accessToken);
            }
        }
    }

    @PostMapping("/reqAccessToken") // 구글 API 액세스 토큰 요청
    public ResponseEntity<?> getAccessToken(@RequestBody Map<String, String> req) {
        Map<String, String> accessToken = new HashMap<>();
        UsersEntity user = usersMapper.findByObserveToken(req.get("observe")).get();
        if (!(user.getInnerId() == null)) {
            accessToken.put("access", googleAPI.getNewAccessTokenByObserve(req.get("observe")));
        } else {
            accessToken.put("access", "0");
        }
        return ResponseEntity.ok(accessToken);
    }

    @PostMapping("/monthly") // 구글 연동 계정 로그인 시, 프론트가 가지고 있는 구글 액세스 토큰과 현재 연월을 기준으로 데이터를 검증
    public void updateMonthly(@RequestBody Map<String, String> req) {
        UsersEntity user = usersMapper.findByObserveToken(req.get("observe")).get();
        if (!(user.getInnerId() == null)) {
            notesDAO.checkGoogleNotes(req.get("observe"), req.get("token"), req.get("date"));
        }
    }

    // 테스트 목적 컨트롤러 메서드
    @ResponseBody
    @PostMapping("/test")
    public void googleTest(@RequestBody Map<String, String> data) {
        System.out.println(data.get("observe"));
        notesDAO.getGoogleNotes(data.get("observe"));
    }
}
