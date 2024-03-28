package e6eo.finalproject.dao

import e6eo.finalproject.dto.CategoryMapper
import e6eo.finalproject.dto.NotesMapper
import e6eo.finalproject.dto.UsersMapper
import e6eo.finalproject.entity.CategoryEntity
import e6eo.finalproject.entity.UsersEntity
import e6eo.finalproject.entityGoogle.GoogleToken
import e6eo.finalproject.entityGoogle.googleUserInfo
import jakarta.annotation.PostConstruct
import lombok.RequiredArgsConstructor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate
import java.util.function.Consumer
import java.util.stream.Collectors

@Service
@RequiredArgsConstructor
open class GoogleAPI() {
    @Value("\${google.key}")
    protected lateinit var googleKey: String

    //  출처: https://ecolumbus.tistory.com/169 [슬기로운 개발자 생활:티스토리]
    @Value("\${google.auth}")
    lateinit var googleAuthUrl: String

    @Value("\${google.redirect}")
    protected lateinit var googleRedirectUrl: String

    @Value("\${google.client.id}")
    protected lateinit var googleClientId: String

    @Value("\${google.client.secret}")
    protected lateinit var googleClientSecret: String

    @Value("\${google.scope}")
    protected lateinit var googleScopeLs: List<String>

    @Autowired
    protected lateinit var usersMapper: UsersMapper

    @Autowired
    protected lateinit var categoryMapper: CategoryMapper

    @Autowired
    protected lateinit var notesMapper: NotesMapper

    @Autowired
    protected lateinit var tokenManager: TokenManager

    protected var usersToken: GoogleToken? = null

    private val logger = LoggerFactory.getLogger(this.javaClass)!!

    // API 요청에 사용되는 기본적인 헤더
    protected fun reqHeader(accessToken: String): Consumer<HttpHeaders> {
        val headers = Consumer { header: HttpHeaders ->
            header.add("Authorization", "Bearer $accessToken")
            header.add("Accept", "application/json")
        }
        return headers
    }

    // 구글 API 권한 목록을 String 으로 변환
    private fun googleScope(): String {
        val scope = StringBuilder()
        for (i in googleScopeLs.indices) {
            scope.append(googleScopeLs[i])
            if (i < googleScopeLs.size - 1) {
                scope.append("%20")
            }
        }
        return scope.toString()
    }

    // 프론트에서 넘겨주는 calendarTitle 데이터를 date 데이터 타입으로 변환하는 메서드
    // calendarTitle은 yyyy-MM 데이터
    // 한달치 데이터를 가져오기 위한 메서드
    protected fun calcDateTime(): Map<String, String> {
        val dateTime: MutableMap<String, String> = HashMap()
        val updateTimeStamp = LocalDate.now().withDayOfMonth(1).atStartOfDay().toString() + ":00Z"
        // 데이터가 조회되는 현재(now)의 월 첫날로 세팅하고(withDayofMonth(1)), 하루를 빼(minusDays(1)) 전 월의 마지막일 설정
        val startTimeStamp =
            LocalDate.now().withDayOfMonth(1).minusDays(1).atStartOfDay().plusHours(9).toString() + ":00Z"
        // 데이터가 조회되는 현재(now)의 월 첫날로 세팅하고(withDayofMonth(1)), 한달을 더해(plusMonths(1)) 전 월의 마지막일 설정
        val endTimeStamp =
            LocalDate.now().withDayOfMonth(1).plusMonths(1).atStartOfDay().plusHours(9).toString() + ":00Z"
        dateTime["update"] = updateTimeStamp
        dateTime["start"] = startTimeStamp
        dateTime["end"] = endTimeStamp
        //        System.out.println(dateTime.get("start"));
//        System.out.println(dateTime.get("end"));
        return dateTime
    }

    // 프론트에서 넘겨주는 calendarTitle 데이터를 date 데이터 타입으로 변환하는 메서드
    // calendarTitle은 yyyy-MM 데이터
    // 3개월치의 데이터를 가져오기 위한 메서드
    protected fun calcDateTime(dateData: String): Map<String, String> {
        // String dateData을 yyyyMM 또는 yyyyM 형태로 변형
        val date = dateData.replace("[^\\d]".toRegex(), "")
        // 위의 데이터에서 연과 월을 추출
        val year = date.substring(0, 4).toInt()
        val month = date.substring(4, date.length).toInt()
        val dateTime: MutableMap<String, String> = HashMap()
        // 현재의 연월에서 한달을 뺀 데이터
        val startTimeStamp = LocalDate.of(year, month, 1).minusMonths(1).atStartOfDay().plusHours(9).toString() + ":00Z"
        // 현재의 연월에서 두달을 더하고 하루를 빼, 다음달의 마지막 일의 데이터를 얻음
        val endTimeStamp =
            LocalDate.of(year, month, 1).plusMonths(2).minusDays(1).atStartOfDay().plusHours(9).toString() + ":00Z"
        dateTime["start"] = startTimeStamp
        dateTime["end"] = endTimeStamp
        //        System.out.println(dateTime.get("start"));
//        System.out.println(dateTime.get("end"));
        return dateTime
    }

    // 구글 계정으로 로그인을 시도했을 때 작동하는 메서드
    fun checkGoogleEmail(): String {
        val userInfo: googleUserInfo = getUserInfo()
        // 해당 구글 계정으로 연동되어있는 계정이 있는지 확인
        val users: UsersEntity? = usersMapper.findByInnerId(userInfo.email)
        // 가입되지 않은 아이디라면
        if (users == null) {
            // 자동 가입 처리
            doAutoSignUp(userInfo)
        } else {
            // 이미 가입되어있는 회원이라면
            // 리프레시토큰의 유효성 검사 및 업데이트 진행
            checkRefreshToken(users)
        }
        // 옵저브 토큰 설정 및 리턴
        return tokenManager.setObserveByInnerId(userInfo.email)
    }


    // 리프레시 토큰의 유효성 검사 및 업데이트 메서드
    protected fun checkRefreshToken(users: UsersEntity) {
        if (usersToken!!.refresh_token != null && users.refreshToken.equals(usersToken!!.refresh_token)) {
            usersMapper.updateRefreshToken(users.userId, usersToken!!.refresh_token)
        }
    }

    // 카테고리 목록 디코딩 -> 카테고리DAO 와 노트DAO 에서 사용 중이라 해당 DAO 에서 작성
    protected fun decodeCategory(categoryList: Array<String>): Map<String, ArrayList<String>> {
        val result: MutableMap<String, ArrayList<String>> = HashMap()
        val calendarLists = ArrayList<String>()
        val taskLists = ArrayList<String>()

        for (category in categoryList) {
//            System.out.println(category);
            val index = category.replace("_", ".").split("\\^".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (index[0] == "google") {
                if (index[1] == "calendar") {
                    calendarLists.add(index[2])
                } else if (index[1] == "tasks") {
                    taskLists.add(index[2])
                }
            }
        }
        result["calendar"] = calendarLists
        result["tasks"] = taskLists
        return result
    }

    // 자동 가입 처리
    private fun doAutoSignUp(userInfo: googleUserInfo) {
        try {
            // 만약 해당 구글 이메일로 가입되어있는 계정이 있다면
            if (!(usersMapper.findById(userInfo.email).isEmpty)) {
                // 해당 데이터의 이너아이디에 저장
                usersMapper.mergeWithInnerId(userInfo.email, userInfo.email, usersToken!!.refresh_token)
                logger.info("Google 계정 연동 완료")
            } else {
                // 해당 구글 이메일로 가입되어있는 계정이 없다면
                val user = UsersEntity(
                    userId = userInfo.email,
                    pw = usersToken!!.access_token!!.substring(0, 19),
                    nickName = userInfo.name,
                    innerId = userInfo.email,
                    refreshToken = usersToken!!.refresh_token
                )
                // System.out.println(user.toString());
                // 가입처리
                usersMapper.save(user)
                val category = CategoryEntity(userId = user.userId!!)
                categoryMapper.save(category)
                categoryMapper.saveDefault(user.userId!!, user.nickName!!)
                logger.info("Google 계정 자동 가입 완료")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logger.info("가입 실패")
        }
    }


    // 구글 계정에서 userInfo 데이터 가져옴
    protected fun getUserInfo(): googleUserInfo {
        val webClient = WebClient.create()
        val userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo"
        val token = usersToken!!.access_token

        // 다른 API의 요청 헤더와 요소가 달라 별도로 생성함
        val headers = Consumer { httpHeaders: HttpHeaders ->
            httpHeaders.add("Authorization", "Bearer $token")
        }

        // 토큰을 userInfo API로 보내 유저 정보를 가져옴
        val userInfo: googleUserInfo =
            webClient.get().uri(userInfoUrl).headers(headers).retrieve().bodyToMono(googleUserInfo::class.java)
                .block()!!
        logger.info(userInfo.toString())
        return userInfo
    }

    // google 로그인 페이지로 이동 및 동의화면 출력하는 메서드
    // 해당 부분이 완료되면 {http://localhost:8080/google/check?code=**&scope=**} 형식으로 Response를 받음
    @PostConstruct
    @Throws(Exception::class)
    fun googleAuthUrlGenerate(): String {
        // 요청 url 생성
        val AUTH_URL = googleAuthUrl
        // 요청 url에 대한 파라미터 생성
        val auth_params: MutableMap<String, Any?> = HashMap()
        auth_params["client_id"] = googleClientId
        auth_params["redirect_uri"] = googleRedirectUrl
        auth_params["response_type"] = "code"
        auth_params["scope"] = googleScope()
        auth_params["access_type"] = "offline"
        auth_params["prompt"] = "consent"
        // 요청 파라미터를 String으로 형변환
        val parameterString = auth_params.entries.stream().map { x: Map.Entry<String, Any?> -> x.key + "=" + x.value }
            .collect(Collectors.joining("&"))
        // 요청 url과 파라미터 결합
        val redirectURL = "$AUTH_URL?$parameterString"

        // 로그 출력으로 확인
//        logger.info("reqUrl : \r\n{}", redirectURL);

        // HttpHeaders 를 사용해 바로 리다이렉션 할 수 있는 경로로 컨트롤러에 전달
//        HttpHeaders redirectReq = new HttpHeaders();
//        redirectReq.setLocation(URI.create(redirectURL));
        //1.redirectReq 구글로그인 창을 띄우고, 로그인 후 /login/check 으로 리다이렉션하게 한다.
        return redirectURL
    }


    // 해당 메서드는 {http://localhost:8080/google/check} 경로에 대해서만 작동하도록 되어있음
    // 경로를 수정해야할 경우 요청할 것
    @Throws(Exception::class)
    fun getGoogleToken(@RequestParam(value = "code") authCode: String?): GoogleToken {
// 출처 : https://dingdingmin-back-end-developer.tistory.com/entry/SpringBoot-%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-Spring-Security-Oauth2-6-Google-Token-%ED%99%9C%EC%9A%A9
        // 토큰 생성 승인 코드가 받아와졌는지 확인
        // System.out.println(authCode);

        // 토큰 요청 경로

        val TOKEN_REQ = "https://oauth2.googleapis.com/token"
        // 토큰 요청에 필요한 내용을 HashMap에 담음
        val token_params: MutableMap<String, Any?> = HashMap()
        token_params["client_id"] = googleClientId
        token_params["client_secret"] = googleClientSecret
        token_params["code"] = authCode
        token_params["grant_type"] = "authorization_code"
        token_params["redirect_uri"] = googleRedirectUrl

        val restTemplate = RestTemplate()

        //3.토큰요청을 한다.
        val apiResponse = restTemplate.postForEntity(TOKEN_REQ, token_params, GoogleToken::class.java)
        //4.받은 토큰을 토큰객체에 저장
        val googleToken = apiResponse.body

        // 토큰이 잘 받아와졌는지 확인
        logger.info("accessTokenBody\r\n{}", googleToken)
        usersToken = googleToken
        return googleToken!!
    }

    // 클라이언트로부터 Google API 요청이 들어왔을 경우
    // 파라미터로 받은 옵저브 토큰으로, 해당 유저의 리프레시 토큰을 받아옴
    // 리프레시 토큰으로 새로운 엑세스 토큰을 발급받아 요청을 진행
    fun getNewAccessTokenByObserve(observeToken: String): String {
        val TOKEN_REQ = "https://oauth2.googleapis.com/token"
        val token_params: MutableMap<String, Any?> = HashMap()
        token_params["client_id"] = googleClientId
        token_params["client_secret"] = googleClientSecret
        // 세션에 있는 User의 데이터의 ID를 사용해서 refresh_token 데이터를 받아오고
        token_params["refresh_token"] = usersMapper.getRefreshTokenByObserve(observeToken)
        token_params["grant_type"] = "refresh_token"
        val restTemplate = RestTemplate()
        // 새로운 토큰을 요청
        val apiResponse = restTemplate.postForEntity(TOKEN_REQ, token_params, GoogleToken::class.java)
        val token = apiResponse.body!!
        logger.info("accessToken\r\n{}", token.access_token)
        // 엑세스 토큰만 리턴하여 바로 사용할 수 있게끔 함
        return token.access_token!!
    }

    // 사후 구글 연동
    fun mergeGoogleAccount(observes: Map<String, String>) {
        val login = usersMapper.findByObserveToken(observes["loginsession"])!!
        val google = usersMapper.findByObserveToken(observes["observe"])!!
        usersMapper.deleteById(google.userId!!)
        usersMapper.mergeWithInnerId(login.userId, google.innerId, google.refreshToken, google.observeToken)
    }
}
