import React, {useEffect, useState} from "react";
import FullCalendar from '@fullcalendar/react';
import listPlugin from '@fullcalendar/list';
import {useNavigate} from "react-router-dom";


export default function LeftBar() {
    const redirect = useNavigate();
    // const [title, setTitle] = useState('');
    // const [startDate, setStartDate] = useState('');
    // const [endDate, setEndDate] = useState('');
    const [events, setEvents] = useState([]);
    const [isCategoryCreateVisible, setIsCategoryCreateVisible] = useState(false); // 1. 새로운 상태 변수 추가
    const [rotationDegree, setRotationDegree] = useState(0); // 1. 회전 각도 상태 변수 추가

    useEffect(() => {
        // 예시로 이벤트를 임의로 생성합니다.
        const exampleEvents = [];
        setEvents(exampleEvents);
    }, []);

    // const openModal = () => {
    //     const dialog = document.getElementById('modal-dialog');
    //     dialog.showModal();
    // };

    // const handleSave = () => {
    //     const newEvent = {
    //         title: title,
    //         start: startDate,
    //         end: endDate
    //     };
    //     setEvents([...events, newEvent]);
    //     onSave(newEvent);
    //     document.getElementById('modal-dialog').close();
    //
    // };

    const [userMenuVisible, setUserMenuVisible] = useState(true);

    const userMenu = document.querySelector(".userMenu");
    const handlerUserMenu = async () => {
        setUserMenuVisible(!userMenuVisible);
        if (userMenuVisible) {
            userMenu.style.setProperty("--userBar-height", "3.3rem");
        } else {
            userMenu.style.setProperty("--userBar-height", "0rem");
        }
    }
    const toggleCategoryCreate = () => {
        setIsCategoryCreateVisible(!isCategoryCreateVisible); // 카테고리 추가 요소의 가시성을 토글
        setRotationDegree(rotationDegree + 45); // 2. 각 클릭마다 45도씩 회전

    };
    const handlerLogout = () => {
        window.sessionStorage.removeItem("observe");
        window.sessionStorage.removeItem("token");
        window.localStorage.removeItem("observe");
        redirect("/");
    }

    return (
        <div className="leftbar">
            {/*<div className={"addevent"}>*/}
            {/*    <button onClick={openModal}>일정추가</button>*/}
            {/*</div>*/}

            <div className="schedule">
                <div className="headLabel">캘린더 리스트
                    <div className="iconButton" onClick={toggleCategoryCreate} style={{transform: `rotate(${rotationDegree}deg)`}}> {/* 회전 각도 적용 */}
                        <svg xmlns="http://www.w3.org/2000/svg" height="100%" viewBox="0 0 40 40" fill="none"
                             stroke="currentColor" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round">
                            <line x1="20" y1="4" x2="20" y2="36"/>
                            <line x1="4" y1="20" x2="36" y2="20"/>
                        </svg>
                    </div>
                </div>
                <ul>
                    <div className="category-create"
                         style={{height: isCategoryCreateVisible ? 'auto' : '0px', overflow: 'hidden'}}>
                        <input style={{opacity: isCategoryCreateVisible ? '1' : '0', transition: 'opacity 0.3s ease'}}/>
                        <button style={{
                            opacity: isCategoryCreateVisible ? '1' : '0',
                            transition: 'opacity 0.3s ease'
                        }}>(캘린더)카테고리 추가
                        </button>
                    </div>
                    <li>경조사</li>
                    <li>출장</li>
                    <li>예비군</li>
                </ul>
            </div>
            <div className="today-tasks">
                <div className="headLabel">오늘 할 일</div>
                {/*<FullCalendar*/}
                {/*    plugins={[listPlugin]}*/}
                {/*    initialView="listDay"*/}
                {/*    events={events}*/}
                {/*    headerToolbar={false}*/}

                {/*/>*/}
            </div>


            <div className={"userbar"}>
                <div className={"userProfile"} onClick={handlerUserMenu}>
                    <div className={"nickName"}>유저 이름</div>
                    <div className={"userButton"}>
                        <svg xmlns="http://www.w3.org/2000/svg" width="100%" height="100%" viewBox="0 0 40 40"
                             fill="currentColor">
                            <circle cx="5" cy="20" r="3"/>
                            <circle cx="20" cy="20" r="3"/>
                            <circle cx="35" cy="20" r="3"/>
                        </svg>
                    </div>
                </div>
                <div className="userMenu">
                    <a href={"/info"} className={"btnFloat"}>내 정보</a>
                    <a className={"btnFloat"} onClick={handlerLogout}>로그아웃</a>
                </div>
            </div>
            {/*<div>*/}
            {/*    <dialog id="modal-dialog">*/}
            {/*        <h2>일정 추가</h2>*/}
            {/*        <label>*/}
            {/*            제목:*/}
            {/*            <input type="text" value={title} onChange={(e) => setTitle(e.target.value)}/>*/}
            {/*        </label>*/}
            {/*        <label>*/}
            {/*            시작 일시:*/}
            {/*            <input type="datetime-local" value={startDate} onChange={(e) => setStartDate(e.target.value)}/>*/}
            {/*        </label>*/}
            {/*        <label>*/}
            {/*            종료 일시:*/}
            {/*            <input type="datetime-local" value={endDate} onChange={(e) => setEndDate(e.target.value)}/>*/}
            {/*        </label>*/}
            {/*        <button onClick={handleSave}>저장</button>*/}
            {/*        <button onClick={() => document.getElementById('modal-dialog').close()}>닫기</button>*/}
            {/*    </dialog>*/}
            {/*</div>*/}
        </div>
    )
}

