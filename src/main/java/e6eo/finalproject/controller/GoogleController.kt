package e6eo.finalproject.controller

import e6eo.finalproject.dao.CategoryDAO
import e6eo.finalproject.dao.GoogleAPI
import e6eo.finalproject.dao.NotesDAO
import e6eo.finalproject.dao.UsersDAO
import e6eo.finalproject.dto.CategoryMapper
import e6eo.finalproject.dto.NotesMapper
import e6eo.finalproject.dto.UsersMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/google")
@CrossOrigin(origins = ["http://localhost:3000"])
class GoogleController {
    @Autowired
    private lateinit var googleAPI: GoogleAPI

    @Autowired
    private lateinit var usersDAO: UsersDAO

    @Autowired
    private lateinit var categoryDAO: CategoryDAO

    @Autowired
    private lateinit var notesDAO: NotesDAO

    @Autowired
    private lateinit var usersMapper: UsersMapper

    @Autowired
    private lateinit var categoryMapper: CategoryMapper

    @Autowired
    private lateinit var notesMapper: NotesMapper

    @GetMapping("/login") // 프론트에서 리다이렉트 될 수 있게끔, ResponseEntity<?> 에 URL을 String 타입으로 담아 반환
    @Throws(Exception::class)
    fun googleOAuth(): ResponseEntity<*> {
        val googleUrl: MutableMap<String, String> = HashMap()
        googleUrl["redirect"] = googleAPI.googleAuthUrlGenerate()
        return ResponseEntity.ok<Map<String, String>>(googleUrl)
    }

    @GetMapping("/check") // /google/login 에서 리다이렉트 되는 경로
    // 파라미터를 추가해 프론트 url 로 전송
    @Throws(Exception::class)
    fun googleCheck(@RequestParam(value = "code") authCode: String): ResponseEntity<Void> {
        googleAPI.getGoogleToken(authCode)
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create("http://localhost:3000/check?autologin=true&observe=" + googleAPI.checkGoogleEmail()))
            .build()
    }

    @PatchMapping("/patch") // 사후 구글 연동
    fun googleAccountMerge(@RequestBody request: Map<String, String>): ResponseEntity<*> {
        googleAPI.mergeGoogleAccount(request)
        return ResponseEntity.ok(true)
    }

    @PostMapping("/update") // 구글 연동 계정 로그인 시, 프론트가 가지고 있는 구글 액세스 토큰과 현재 연월을 기준으로 데이터를 검증
    fun updateMonthly(@RequestBody req: Map<String, String>) {
        val user = usersMapper.findByObserveToken(req["observe"])!!
        if (user.innerId != null) {
            notesDAO.checkGoogleNotes(req["observe"], req["token"]!!, req["date"]!!)
        }
    }

    @PostMapping("/updateCheck")
    fun checkGoogleAccount(@RequestBody observe: String) {
        // System.out.println("구글 데이터 업데이트");
        val observeToken = observe.replace("\"", "")
        val user = usersMapper.findByObserveToken(observeToken)!!
        println(user)
        var accessToken: String? =
            if (user.innerId != null) googleAPI.getNewAccessTokenByObserve(observeToken) else null
        if (accessToken != null) {
            if (categoryDAO.checkGoogleCategory(user, accessToken)) {
                // 구글 데이터를 받아온 적이 없는 경우
                // 데이터를 요청해서 받아옴
                notesDAO.getGoogleNotes(user, accessToken)
            }
        }
    }

    @PostMapping("/reqAccessToken") // 구글 API 액세스 토큰 요청
    fun getAccessToken(@RequestBody req: Map<String, String>): ResponseEntity<*> {
        val accessToken: MutableMap<String, String> = HashMap()
        val user = usersMapper.findByObserveToken(req["observe"])!!
        if (user.innerId != null) {
            accessToken["access"] = googleAPI.getNewAccessTokenByObserve(req["observe"]!!)
        } else {
            accessToken["access"] = "0"
        }
        return ResponseEntity.ok<Map<String, String>>(accessToken)
    }

    // 테스트 목적 컨트롤러 메서드
    @ResponseBody
    @PostMapping("/test")
    fun googleTest(@RequestBody data: Map<String, String>) {
        println(data["observe"])
    }
}
