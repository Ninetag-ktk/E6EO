package e6eo.finalproject.dao

import e6eo.finalproject.entity.UsersEntity
import e6eo.finalproject.entityGoogle.googleLists
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.util.*

@Service
@RequiredArgsConstructor
class CategoryDAO : GoogleAPI() {
    // /categories 요청을 받았을 때
    // 카테고리 목록 return
    fun categoriesGet(observe: String): Map<String, String> {
        // System.out.println(request);
        val user = usersMapper.findByObserveToken(observe)!!
        val categories: Map<String, String> = categoryMapper.findById(user.userId!!).get().categories!!
        (categories.values as MutableCollection).removeAll(mutableListOf(""))
//        println(categories)
        return categories
    }

    // **구글 연동 계정이 로그인했을 경우**, 해당 계정의 구글 카테고리를 get / update
    fun checkGoogleCategory(user: UsersEntity, accessToken: String): Boolean {
        // user 데이터에서 id를 받아 카테고리 목록을 조회 = decode 되지 않은 카테고리 id 목록
        val category = categoryMapper.findById(user.userId!!).get().categories
        // 만약 카테고리 목록이 비어있다면
        if (category == null) {
            // 기본적인 카테고리(e6eo)를 insert
            categoryMapper.saveDefault(user.userId!!, user.nickName!!)
            // 이후 구글 카테고리 목록을 insert
            getGoogleCategory(user.userId!!, accessToken)
            return true
        } else {
            // 우선 리스트 목록을 조회
            // keySet.toArray == 카테고리 id를 String 배열로 반환
            val categories = category.keys.toTypedArray<String>()
            // 카테고리 목록에 없다면 데이터를 추가
            scanCategory(user, accessToken, categories)
            // categories를 decode
            // categoryMap 의 value는 String[]
            val categoryMap = decodeCategory(categories)
            // calendar와 tasks의 배열의 사이즈가 0이라면 == 구글에서 카테고리를 가져온 적이 없음
            // categories는 데이터를 추가하기 전에 선언한 변수이기 때문에 가능
            // 둘 중 하나라도 비어있다면 true를 반환하여 getGoogleNotes를 수행
            // 둘 다 비어있지 않다면 false를 반환하여 note의 etag 검사로 넘어감
            return if (categoryMap["calendar"]?.isNullOrEmpty()!! || categoryMap["tasks"]?.isNullOrEmpty()!!) true else false
        }
    }

    // 구글 카테고리 목록을 데이터베이스에 저장
    fun getGoogleCategory(userId: String, accessToken: String): Map<String, String> {
        val categories: MutableMap<String, String> = HashMap()
        // accesstoken 으로 캘린더 목록을 받아 Map 데이터에 담음 = decoding 됨
        categories.putAll(listCalendar(accessToken))
        // accesstoken 으로 태스크 목록을 받아 Map 데이터에 담음 = decoding 됨
        categories.putAll(listTasks(accessToken))
        // 모든 데이터를 각각 데이터베이스에 저장
        for (key in categories.keys) {
            categoryMapper.addCategory(userId, key, categories[key]!!)
            // System.out.println(key + "  :  " + category.get(key));
        }
        return categories
    }

    // 구글 카테고리 목록을 데이터베이스에 저장 == 옵저브 토큰으로 진행
    fun getGoogleCategoryByObserve(observe: String): Map<String, String> {
        val user = usersMapper.findByObserveToken(observe)
        val categories: MutableMap<String, String> = HashMap()
        if (user == null) {
            categories["error"] = "NoAuthorizedAccess"
            return categories
        }
        val accessToken = getNewAccessTokenByObserve(observe)
        categories.putAll(listCalendar(accessToken))
        categories.putAll(listTasks(accessToken))
        for (key in categories.keys) {
            categoryMapper.addCategory(user.userId!!, key, categories[key]!!)
            // System.out.println(key + "  :  " + category.get(key));
        }
        return categories
    }

    // accessToken 으로 구글 캘린더의 목록을 받아옴
    private fun listCalendar(accessToken: String): Map<String, String> {
        val webClient = WebClient.create()
        val Url = "https://www.googleapis.com/calendar/v3/users/me/calendarList?maxResults=100&key=$googleKey"
        val json =
            webClient.get().uri(Url).headers(reqHeader(accessToken)).retrieve().bodyToMono(googleLists::class.java)
                .block()!!.items
        val category: MutableMap<String, String> = HashMap()
        // System.out.println(json);
        for (item in json as ArrayList<Map<*, *>>) {
            // # 이 들어가는 대한민국의 공휴일과 생일 데이터는 데이터 적재 및 프론트 출력 과정에 에러가 발생.
            // 해당 데이터는 데이터베이스에 적재하지 않음
            if (item["id"] == "ko.south_korea#holiday@group.v.calendar.google.com" || item["id"] == "addressbook#contacts@group.v.calendar.google.com") continue
            else if (item["id"] == item["summary"]) { // 만약 구글 아이디와 캘린더 아이디가 같다면 == primary => 내 구글 캘린더로 저장
                category["google^calendar^" + item["id"].toString().replace(".", "_")] = "내 구글 캘린더"
            } else {
                category["google^calendar^" + item["id"].toString().replace(".", "_")] = item["summary"].toString()
            }
        }
        return category
    }

    // accessToken 으로 구글 태스크의 목록을 받아옴
    private fun listTasks(accessToken: String): Map<String, String> {
        val webClient = WebClient.create()
        val Url = "https://tasks.googleapis.com/tasks/v1/users/@me/lists?maxResults=100&key=$googleKey"
        val json =
            webClient.get().uri(Url).headers(reqHeader(accessToken)).retrieve().bodyToMono(googleLists::class.java)
                .block()!!.items
        val category: MutableMap<String, String> = HashMap()
        // System.out.println(json);
        for (item in json as ArrayList<Map<*, *>>) {
            category["google^tasks^" + item["id"].toString().replace(".", "_")] = item["title"].toString()
        }
        return category
    }

    // 현재 데이터베이스에 있는 카테고리와 구글에서 받아오는 카테고리 목록을 비교 후 업데이트
    // DB에서 가져온 decoding 되지 않은 카테고리 목록을 파라미터로 받음
    private fun scanCategory(user: UsersEntity, accessToken: String, categories: Array<String>) {
        val calendarList = listCalendar(accessToken)
        val tasksList = listTasks(accessToken)
        for (categoryId in calendarList.keys.toTypedArray<String>()) {
            if (!Arrays.asList(*categories).contains(categoryId)) {
                categoryMapper.addCategory(user.userId!!, categoryId, calendarList[categoryId]!!)
            }
        }
        for (categoryId in tasksList.keys.toTypedArray<String>()) {
            if (!Arrays.asList(*categories).contains(categoryId)) {
                categoryMapper.addCategory(user.userId!!, categoryId, tasksList[categoryId]!!)
            }
        }
    }

    fun updateCategory(request: Map<String, String>) {
        val user = usersMapper.findByObserveToken(request["observe"])!!
        categoryMapper.updateCategory(user.userId!!, request["key"]!!, request["value"]!!)
    }

    fun deleteCategory(request: Map<String, String>) {
        val user = usersMapper.findByObserveToken(request["observe"])!!
        if (request["key"]!!.startsWith("google")) {
            categoryMapper.deleteCategoryGoogle(user.userId!!, request["key"]!!)
        } else {
            categoryMapper.deleteCategory(user.userId!!, request["key"]!!)
        }
        notesMapper.deleteByCategoryId(user.userId!!, request["key"]!!)
    }
}
