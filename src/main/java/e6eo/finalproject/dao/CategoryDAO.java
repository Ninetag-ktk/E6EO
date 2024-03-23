package e6eo.finalproject.dao;

import e6eo.finalproject.entity.CategoryEntity;
import e6eo.finalproject.entity.UsersEntity;
import e6eo.finalproject.entityGoogle.googleLists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryDAO extends GoogleAPI {

    // /categories 요청을 받았을 때
    // 카테고리 목록 return
    public Map<String, String> categoriesGet(String observe) {
        // System.out.println(request);
        UsersEntity user = usersMapper.findByObserveToken(observe).get();
        Map<String, String> categories = categoryMapper.findById(user.getUserId()).get().getCategories();
        // System.out.println(categories);
        return categories;
    }

    // **구글 연동 계정이 로그인했을 경우**, 해당 계정의 구글 카테고리를 get / update
    public boolean checkGoogleCategory(UsersEntity user, String accessToken) {
        // user 데이터에서 id를 받아 카테고리 목록을 조회 = decode 되지 않은 카테고리 id 목록
        Optional<CategoryEntity> category = categoryMapper.findById(user.getUserId());
        // 만약 카테고리 목록이 비어있다면
        if (category.isEmpty()) {
            // user id 를 id 로 갖는 데이터 하나를 데이터에 추가
            categoryMapper.insert(new CategoryEntity().builder().userId(user.getUserId()).build());
            // 기본적인 카테고리(e6eo)를 insert
            categoryMapper.insertDefault(user.getUserId(), user.getNickName());
            // 이후 구글 카테고리 목록을 insert
            getGoogleCategory(user.getUserId(), accessToken);
            return true;
        } else {
            String[] categories = categoryMapper.findById(user.getUserId())
                    .get().getCategories().keySet().toArray(new String[0]);
            // 우선 리스트 목록을 조회
            // 카테고리 목록에 없다면 데이터를 추가
            scanCategory(user, accessToken, categories);
            // categories를 decode
            // categoryMap 의 value는 String[]
            Map<String, ArrayList<String>> categoryMap = decodeCategory(categories);
            // calendar와 tasks의 배열의 사이즈가 0이라면 == 구글에서 카테고리를 가져온 적이 없음
            // categories는 데이터를 추가하기 전에 선언한 변수이기 때문에 가능
            // 둘 중 하나라도 비어있다면 true를 반환하여 getGoogleNotes를 수행
            // 둘 다 비어있지 않다면 false를 반환하여 note의 etag 검사로 넘어감
            return (categoryMap.get("calendar").size() == 0 || categoryMap.get("tasks").size() == 0) ? true : false;
        }
    }

    // 구글 카테고리 목록을 데이터베이스에 저장
    public Map<String, String> getGoogleCategory(String userId, String accessToken) {
        Map<String, String> categories = new HashMap<>();
        // accesstoken 으로 캘린더 목록을 받아 Map 데이터에 담음 = decoding 됨
        categories.putAll(listCalendar(accessToken));
        // accesstoken 으로 태스크 목록을 받아 Map 데이터에 담음 = decoding 됨
        categories.putAll(listTasks(accessToken));
        // 모든 데이터를 각각 데이터베이스에 저장
        for (String key : categories.keySet()) {
            categoryMapper.addCategory(userId, key, categories.get(key));
            // System.out.println(key + "  :  " + category.get(key));
        }
        return categories;
    }

    // 구글 카테고리 목록을 데이터베이스에 저장 == 옵저브 토큰으로 진행
    public Map<String, String> getGoogleCategoryByObserve(String observe) {
        Optional<UsersEntity> user = usersMapper.findByObserveToken(observe);
        Map<String, String> categories = new HashMap<>();
        if (user.isEmpty()) {
            categories.put("error", "NoAuthorizedAccess");
            return categories;
        }
        String accessToken = getNewAccessTokenByObserve(observe);
        categories.putAll(listCalendar(accessToken));
        categories.putAll(listTasks(accessToken));
        for (String key : categories.keySet()) {
            categoryMapper.addCategory(user.get().getUserId(), key, categories.get(key));
            // System.out.println(key + "  :  " + category.get(key));
        }
        return categories;
    }

    // accessToken 으로 구글 캘린더의 목록을 받아옴
    private Map<String, String> listCalendar(String accessToken) {
        WebClient webClient = WebClient.create();
        String Url = "https://www.googleapis.com/calendar/v3/users/me/calendarList?maxResults=100&key=" + googleKey;
        Object json = webClient.get().uri(Url).headers(reqHeader(accessToken)).retrieve().bodyToMono(googleLists.class).block().getItems();
        Map<String, String> category = new HashMap<>();
        // System.out.println(json);
        for (Map item : (ArrayList<Map>) json) {
            // # 이 들어가는 대한민국의 공휴일과 생일 데이터는 데이터 적재 및 프론트 출력 과정에 에러가 발생.
            // 해당 데이터는 데이터베이스에 적재하지 않음
            if (item.get("id").equals("ko.south_korea#holiday@group.v.calendar.google.com") || item.get("id").equals("addressbook#contacts@group.v.calendar.google.com"))
                continue;
            else if (item.get("id").equals(item.get("summary"))) { // 만약 구글 아이디와 캘린더 아이디가 같다면 == primary => 내 구글 캘린더로 저장
                category.put("google^calendar^" + item.get("id").toString().replace(".", "_"), "내 구글 캘린더");
            } else {
                category.put("google^calendar^" + item.get("id").toString().replace(".", "_"), item.get("summary").toString());
            }
        }
        return category;
    }

    // accessToken 으로 구글 태스크의 목록을 받아옴
    private Map<String, String> listTasks(String accessToken) {
        WebClient webClient = WebClient.create();
        String Url = "https://tasks.googleapis.com/tasks/v1/users/@me/lists?maxResults=100&key=" + googleKey;
        Object json = webClient.get().uri(Url).headers(reqHeader(accessToken)).retrieve().bodyToMono(googleLists.class).block().getItems();
        Map<String, String> category = new HashMap<>();
        // System.out.println(json);
        for (Map item : (ArrayList<Map>) json) {
            category.put("google^tasks^" + item.get("id").toString().replace(".", "_"), item.get("title").toString());
        }
        return category;
    }

    // 현재 데이터베이스에 있는 카테고리와 구글에서 받아오는 카테고리 목록을 비교 후 업데이트
    // DB에서 가져온 decoding 되지 않은 카테고리 목록을 파라미터로 받음
    private void scanCategory(UsersEntity user, String accessToken, String[] categories) {
        Map<String, String> calendarList = listCalendar(accessToken);
        Map<String, String> tasksList = listTasks(accessToken);
        for (String categoryId : calendarList.keySet().toArray(new String[0])) {
            if (!Arrays.asList(categories).contains(categoryId)) {
                categoryMapper.addCategory(user.getUserId(), categoryId, calendarList.get(categoryId));
            }
        }
        for (String categoryId : tasksList.keySet().toArray(new String[0])) {
            if (!Arrays.asList(categories).contains(categoryId)) {
                categoryMapper.addCategory(user.getUserId(), categoryId, tasksList.get(categoryId));
            }
        }
    }
}
