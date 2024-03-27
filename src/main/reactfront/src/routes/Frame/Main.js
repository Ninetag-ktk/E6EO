// Main.js
import React, {useEffect, useRef, useState} from "react";
import Header from "./Header";
import LeftBar from "./LeftBar";
import Search from "./Outlet/Search";
import Center from "./Outlet/Center";
import {useNavigate} from "react-router-dom";
import axios from "axios";

export const MyContext = React.createContext();

export default function Main() {
    const [calendar, setCalendar] = useState(null);
    const [calendarTitle, setCalendarTitle] = useState('');
    const [categories, setCategories] = useState([]);
    const [categoryLoading, setCategoryLoading] = useState(true);
    const [events, setEvents] = useState([]);
    const [isSearchVisible, setIsSearchVisible] = useState(false);
    const [userName, setUserName] = useState('')
    const redirect = useNavigate();
    const apiRequestRef = useRef(0);
    const noteRef = useRef();

    useEffect(() => {
        /*만약 정상적인 로그인이 아니라면 == 세션에 데이터가 없다면*/
        if (window.sessionStorage.getItem("observe") == null) {
            /*홈 화면으로 튕겨냄*/
            redirect("/");
        } else {
            getUserName();
        }
        const interval = setInterval(() => {
            if (window.sessionStorage.getItem("token") != null) {
                checkToken();
            }
        }, 10000);
        dateSetting();
        categoryListData();
    }, []);

    /*날짜 데이터가 바뀌면 해당 내용을 서버로 보내 미리 데이터를 받아올 수 있게끔 실행*/
    useEffect(() => {
        getNoteList(calendarTitle);
        // api 요청이 너무 자주 가지 않게끔 useRef로 컨트롤
        noteListDate(calendarTitle);
        apiRequestRef.current = Date.now();
    }, [calendarTitle]);

    useEffect(() => {
        getNoteList(calendarTitle);

        // api 요청이 너무 자주 가지 않게끔 useRef로 컨트롤
        const now = Date.now();
        const lastRunTime = apiRequestRef.current;
        // 1분 주기로 실행되게끔
        if (now - lastRunTime >= 60000) {
            noteListDate(calendarTitle);
            apiRequestRef.current = now;
        }
    }, [categories])

    // 카테고리 리스트를 백엔드에서 받아오고, indexedDB에서도 가져옴  
    const categoryListData = async () => {
        setCategoryLoading(true);
        // 카테고리 목록을 백엔드 서버에서 가져옴
        const response = await axios.post("/categories", window.sessionStorage.getItem("observe"));
        // console.log(response)
        // 백엔드에서 받아온 카테고리 목록을 categoriesGet 변수에 담음
        const categoriesGet = Object.entries(response.data);
        // indexedDB 오픈
        const indexDB = window.indexedDB.open("e6eo");
        indexDB.onerror = (event) => {
            console.error("데이터베이스 열기 실패:", event.target.error);
        };
        // indexedDB에 스토리지가 없다면 스토리지 생성
        // index 데이터 없이, 그냥 key-value 쌍으로 이루어진 데이터 셋
        indexDB.onupgradeneeded = (event) => {
            const db = event.target.result;
            const objectStore = db.createObjectStore("categories_checked", {keyPath: "categoryId"});
        };
        // indexedDB의 categories_checked 스토리지가 열리면, 아래 프로세스 진행
        indexDB.onsuccess = (event) => {
            // console.log("데이터베이스 열기 성공")
            const db = event.target.result;
            const transaction = db.transaction("categories_checked", "readwrite");
            const objectStore = transaction.objectStore("categories_checked");
            const categoryList = categoriesGet.map((category) => {
                return new Promise((resolve) => {
                    const categoryData = objectStore.get(category[0]);
                    categoryData.onsuccess = (event) => {
                        const data = event.target.result;
                        // 백엔드에서 받아온 데이터가 indexedDB에 없다면, 값을 삽입(put)
                        if (data === undefined) {
                            // 기본값 설정
                            objectStore.put({categoryId: category[0], value: true});
                            // console.log([category[0], category[1], true]);
                            resolve([category[0], category[1], true]);
                        } else {
                            resolve([category[0], category[1], data.value]);
                        }
                    };
                });
                // console.log("체크 안쪽", categoryList)
            })
            // 위 작업이 모두 성공하면(Promise.all) 해당 값을 result 에 담고, setCategories
            Promise.all(categoryList).then((results) => {
                setCategories(results);
                setCategoryLoading(false);
                // console.log("데이터 체크 :", results);
            })
        }
    }

    function checkToken() {
        const token = JSON.parse(window.sessionStorage.getItem("token"))
        // console.log(token.expire);
        // console.log(new Date().getTime());
        if (new Date().getTime() >= token.expire) {
            // console.log("토큰삭제");
            // window.sessionStorage.removeItem("token");
            getToken();
        }
    }

    function dateSetting() {
        // 초기 렌더링 시점에 값을 설정하는 로직 추가
        const today = new Date();
        const options = {year: "numeric", month: "long"};
        const formattedDate = today.toLocaleDateString("ko-KR", options);
        setCalendarTitle(formattedDate);
    }

    const getNoteList = async (calendarTitle) => {
        if (calendarTitle !== '' && categories.length > 0) {
            await axios.post("/notes",
                {
                    observe: sessionStorage.getItem("observe"),
                    date: calendarTitle,
                    categoryData: categories,
                }
            ).then((response) => {
                // console.log(response.data);
                // console.log(data, typeof data);
                const fullEvents = response.data.map((note) => {
                    if (note.type === "task" || note.type === "memo") {
                        return {
                            id: note.id,
                            title: note.title,
                            start: note.startTime,
                            end: note.endTime,
                            allDay: true,
                            type: note.type,
                            data: note,
                        }
                    } else {
                        return {
                            id: note.id,
                            title: note.title,
                            start: note.startTime,
                            end: note.endTime,
                            type: note.type,
                            data: note,
                        }
                    }
                })
                setEvents(fullEvents);
            })
        }
    }

    function getToken() {
        let returnResult;
        fetch("/google/reqAccessToken", {
            method: "POST",
            headers: {
                "Content-Type": "application/json; charset=utf-8",
                "Accept": "application/json; charset=utf-8",
            },
            body: JSON.stringify({
                observe: window.sessionStorage.getItem("observe"),
            }),
        }).then(response => response.json())
            .then((result) => {
                window.sessionStorage.setItem("token", JSON.stringify({
                    access: result.access,
                    expire: new Date().getTime() + (1000 * 60 * 10),
                }))
                returnResult = result
            })
        return returnResult;
    }

    const getUserName = async () => {
        const userName = await axios.post("/user", window.sessionStorage.getItem("observe"));
        setUserName(userName.data);
    }

    const gotoday = () => {
        if (calendar) {
            calendar.today();
            setTitle(calendar.view.title);
        }
    }

    const handleSaveEvent = (event) => {
        setEvents([...events, event]);
    }

    /*캘린더-검색창 전환을 위한 기능*/
    function handleToggle() {
        setIsSearchVisible(!isSearchVisible);
    }

    const handlePrevButtonClick = () => {
        if (calendar) {
            calendar.prev();
            setTitle(calendar.view.title);
        }
    }

    const handleNextButtonClick = () => {
        if (calendar) {
            calendar.next();
            setTitle(calendar.view.title);
        }
    }

    // 선택되어있는 월을 기준으로 3개월치의 데이터를 백업하는 메서드
    async function noteListDate(calendarTitle) {
        let token = null;
        try {
            if (JSON.parse(window.sessionStorage.getItem("token"))) {
                token = JSON.parse(window.sessionStorage.getItem("token")).access;
            } else {
                token = await getToken().access;
            }
        } catch (err) {
            // console.log("토큰 오류:", err.message);
            return;
        }
        // console.log("토큰:", JSON.parse(window.sessionStorage.getItem("token")));
        if (calendarTitle !== "") {
            await axios("/google/update", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json; charset=utf-8",
                    "Accept": "application/json; charset=utf-8",
                },
                data: JSON.stringify({
                    observe: window.sessionStorage.getItem("observe"),
                    token: token,
                    date: calendarTitle,
                }),
            }).catch(e => console.log("에러 : ", e));
        }
        await getNoteList(calendarTitle);
    }

    const setTitle = (title) => {
        setCalendarTitle(title);
    }

    return (
        <div className={"main"}>
            <MyContext.Provider value={{isSearchVisible, handleToggle}}>
                <div className={"frame"}>
                    <Header
                        onPrevButtonClick={handlePrevButtonClick}
                        onNextButtonClick={handleNextButtonClick}
                        today={gotoday}
                        currentTitle={calendarTitle}
                    />
                    <div className={"container"}>
                        <LeftBar categories={categories} setCategories={setCategories}
                                 categoryLoading={categoryLoading} userName={userName}/>
                        {isSearchVisible ? <Search/> :
                            <Center setMainCalendar={setCalendar} setTitle={setTitle} events={events}
                                    setEvents={setEvents} onSave={handleSaveEvent}
                                    noteRef={noteRef} categories={categories}
                                    noteListDate={noteListDate} calendarTitle={calendarTitle}/>}
                    </div>
                </div>
            </MyContext.Provider>
        </div>
    );
}
