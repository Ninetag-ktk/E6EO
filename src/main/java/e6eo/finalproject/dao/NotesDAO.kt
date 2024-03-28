package e6eo.finalproject.dao;

import e6eo.finalproject.entity.CategoryEntity;
import e6eo.finalproject.entity.NoteData;
import e6eo.finalproject.entity.NotesEntity;
import e6eo.finalproject.entity.UsersEntity;
import e6eo.finalproject.entityGoogle.googleLists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotesDAO extends GoogleAPI {

    // 전달받은 월 데이터 기준, 앞뒤를 포함해 3개월치의 데이터를 체크
    public void checkGoogleNotes(String observe, String accessToken, String date) {
        UsersEntity user = usersMapper.findByObserveToken(observe).get();
        List<NotesEntity> notes = notesMapper.findByUserId(user.getUserId());
        Map<Object, Object> notesEtag = notes.stream().collect(Collectors.toMap(NotesEntity::getId, NotesEntity::getEtag));
        Optional<CategoryEntity> category = categoryMapper.findById(user.getUserId());
        Map<String, ArrayList<String>> categories = new HashMap<>();
        if (!(category.isEmpty())) {
            categories = decodeCategory(category.get().getCategories().keySet().toArray(new String[0]));
        } else {
//            System.out.println("카테고리 비어있음");
        }
        scanCalendarNotes(notesEtag, noteCalendar(user.getUserId(), categories.get("calendar"), accessToken, date));
        scanTasksNotes(notesEtag, noteTasks(user.getUserId(), categories.get("tasks"), accessToken, date));
    }

    private void scanCalendarNotes(Map<Object, Object> notesEtag, ArrayList<NotesEntity> noteCalendar) {
        int i = 0;
        for (NotesEntity note : noteCalendar) {
//            System.out.println(note);
            if (note.getStatus().toString().equals("cancelled") && notesEtag.get(note.getId()) != null) {
//                System.out.println(note);
                notesMapper.delete(note);
                i++;
            } else if (!note.getStatus().toString().equals("cancelled") && !note.getEtag().equals(notesEtag.get(note.getId()))) {
//                System.out.println(note);
                notesMapper.save(note);
                i++;
            }
        }
//        System.out.println("캘린더 event 업데이트 개수 : " + i);
    }

    private void scanTasksNotes(Map<Object, Object> notesEtag, ArrayList<NotesEntity> noteTasks) {
        int i = 0;
        for (NotesEntity note : noteTasks) {
//            System.out.println(note);
            if (note.getStatus().toString().equals("cancelled") && notesEtag.get(note.getId()) != null) {
//                System.out.println(note);
                notesMapper.delete(note);
                i++;
            } else if (!note.getStatus().toString().equals("cancelled") && !note.getEtag().equals(notesEtag.get(note.getId()))) {
//                System.out.println(note);
                notesMapper.save(note);
                i++;
            }
        }
//        System.out.println("태스트 task 업데이트 개수 : " + i);
    }

    // 데이터 최초로 가져오기
    public void getGoogleNotes(UsersEntity user, String accessToken) {
        String[] categories = categoryMapper.findById(user.getUserId()).get().getCategories().keySet().toArray(new String[0]);
        Map<String, ArrayList<String>> categoryMap = decodeCategory(categories);
        notesMapper.saveAll(noteCalendar(user.getUserId(), categoryMap.get("calendar"), accessToken));
        notesMapper.saveAll(noteTasks(user.getUserId(), categoryMap.get("tasks"), accessToken));
        System.out.println("GetNotesFromGoogle_Complete");
    }

    private ArrayList<NotesEntity> noteCalendar(String userId, ArrayList<String> list, String accessToken) {
        ArrayList<NotesEntity> notes = new ArrayList<>();
        WebClient webClient = WebClient.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                .baseUrl("https://www.googleapis.com/calendar/v3/calendars/")
                .defaultHeaders(reqHeader(accessToken))
                .build();
        Map<String, String> dateTime = calcDateTime();
        String requestUrl = null;
        // date 가 없으면 showDeleted = false
        requestUrl = "/events?orderBy=updated&showDeleted=false&singleEvents=true"
                + "&timeMin=" + dateTime.get("start")
                + "&timeMax=" + dateTime.get("end")
                + "&timeZone=GMT+9"
//                + "&updatedMin=" + dateTime.get("update")
                + "&key=" + googleKey;
        for (String calendar : list) {
            Object json = webClient.get().uri(calendar + requestUrl)
                    .retrieve().bodyToMono(googleLists.class).block().getItems();
            for (Map<String, Object> event : (ArrayList<Map>) json) {
                NotesEntity note = new NotesEntity().eventParser(event, userId, calendar);
                notes.add(note);
            }
        }
        return notes;
    }

    private ArrayList<NotesEntity> noteCalendar(String userId, ArrayList<String> list, String accessToken, String date) {
        ArrayList<NotesEntity> notes = new ArrayList<>();
        WebClient webClient = WebClient.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                .baseUrl("https://www.googleapis.com/calendar/v3/calendars/")
                .defaultHeaders(reqHeader(accessToken))
                .build();
        Map<String, String> dateTime = calcDateTime(date);
        String requestUrl = "/events?orderBy=updated"
                + "&showDeleted=true&singleEvents=true"
                + "&timeMin=" + dateTime.get("start")
                + "&timeMax=" + dateTime.get("end")
                + "&timeZone=GMT+9&key=" + googleKey;
        for (String calendar : list) {
            Object json = webClient.get().uri(calendar + requestUrl)
                    .retrieve().bodyToMono(googleLists.class).block().getItems();
            for (Map<String, Object> event : (ArrayList<Map>) json) {
                NotesEntity note = new NotesEntity().eventParser(event, userId, calendar);
                notes.add(note);
            }
        }
        return notes;
    }

    private ArrayList<NotesEntity> noteTasks(String userId, ArrayList<String> list, String accessToken) {
        ArrayList<NotesEntity> notes = new ArrayList<>();
        WebClient webClient = WebClient.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                .baseUrl("https://tasks.googleapis.com/tasks/v1/lists/")
                .defaultHeaders(reqHeader(accessToken))
                .build();
        Map<String, String> dateTime = calcDateTime();
        String requestUrl = null;
        // date 가 없으면 showDeleted = false
        requestUrl = "/tasks"
                + "?dueMax=" + dateTime.get("end")
                + "&dueMin=" + dateTime.get("start")
                + "&showCompleted=true&showDeleted=false&showHidden=true"
//                + "&updatedMin=" + dateTime.get("update")
                + "&key" + googleKey;
        for (String tasklist : list) {
            Object json = webClient.get().uri(tasklist + requestUrl)
                    .retrieve().bodyToMono(googleLists.class).block().getItems();
            for (Map<String, Object> task : (ArrayList<Map>) json) {
                NotesEntity note = new NotesEntity().taskParser(task, userId, tasklist);
                notes.add(note);
            }
        }
        return notes;
    }

    private ArrayList<NotesEntity> noteTasks(String userId, ArrayList<String> list, String accessToken, String date) {
        ArrayList<NotesEntity> notes = new ArrayList<>();
        WebClient webClient = WebClient.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
                .baseUrl("https://tasks.googleapis.com/tasks/v1/lists/")
                .defaultHeaders(reqHeader(accessToken))
                .build();
        Map<String, String> dateTime = calcDateTime(date);
        String requestUrl = "/tasks"
                + "?dueMax=" + dateTime.get("end")
                + "&dueMin=" + dateTime.get("start")
                + "&showCompleted=true&showDeleted=true&showHidden=true"
                + "&key" + googleKey;
        for (String tasklist : list) {
            Object json = webClient.get().uri(tasklist + requestUrl)
                    .retrieve().bodyToMono(googleLists.class).block().getItems();
            for (Map<String, Object> task : (ArrayList<Map>) json) {
                NotesEntity note = new NotesEntity().taskParser(task, userId, tasklist);
                notes.add(note);
            }
        }
        return notes;
    }

    public List<NotesEntity> notesGet(Map<String, Object> request) {
        UsersEntity user = usersMapper.findByObserveToken(request.get("observe").toString()).get();
//        System.out.println(user);
        Map<String, String> dateRange = calcDateTime(request.get("date").toString());
        List<List<Object>> categoryData = (List<List<Object>>) request.get("categoryData");
        ArrayList<String> categoryIds = new ArrayList<>();
        for (List<Object> category : categoryData) {
            if ((boolean) category.get(2) == true) {
                if (category.get(0).toString().contains("google")) {
                    String[] categoryCode = category.get(0).toString().split("\\^");
                    categoryIds.add(categoryCode[categoryCode.length - 1].replaceAll("_", "."));
                } else {
                    categoryIds.add(category.get(0).toString());
                }
            }
        }
//        System.out.println(categoryIds);
        List<NotesEntity> notesList = new ArrayList<NotesEntity>();
        for (String categoryId : categoryIds) {
            notesList.addAll(notesMapper.getNotes(user.getUserId(), dateRange.get("start"), dateRange.get("end"), categoryId));
        }
//        System.out.println(notesList);
        return notesList;
    }

    public void insertNote(NoteData noteData) {
        UsersEntity user = usersMapper.findByObserveToken(noteData.getObserve()).get();
        NotesEntity note = null;
        if (noteData.getCategoryId() != null) {
            switch (noteData.getNote().get("kind").toString().split("#")[1]) {
                case "event":
                    note = new NotesEntity().eventParser(noteData.getNote(), user.getUserId(), noteData.getCategoryId());
                    break;
                case "task":
                    note = new NotesEntity().taskParser(noteData.getNote(), user.getUserId(), noteData.getCategoryId());
                    break;
            }
        } else {
            note = new NotesEntity().dataParser(noteData.getNote(), user.getUserId());
        }
        notesMapper.save(note);
        System.out.println("체크 : " + note);
    }

    public void deleteNote(NoteData noteData) {
        UsersEntity user = usersMapper.findByObserveToken(noteData.getObserve()).get();
        // 유저 아이디를 기준으로 모든 노트 삭제
        notesMapper.deleteByIdWithUserId(noteData.getNote().get("id").toString(), user.getUserId());
    }
}
