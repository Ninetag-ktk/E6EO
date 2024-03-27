import React, {useState} from "react";
import {useNavigate} from "react-router-dom";
import axios from "axios";


export default function LeftBar({categories, setCategories, categoryLoading, userName}) {
    const redirect = useNavigate();
    const [title, setTitle] = useState('');
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [events, setEvents] = useState([]);
    const [isCategoryCreateVisible, setIsCategoryCreateVisible] = useState(false); // 1. 새로운 상태 변수 추가
    const [rotationDegree, setRotationDegree] = useState(0); // 1. 회전 각도 상태 변수 추가

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
        deleteCategoryListData();
        redirect("/");
    }

    // 카테고리의 체크박스 설정이 바뀌면 indexedDB 의 데이터 수정
    function categoryHandler(e) {
        if (e.target.getAttribute("type") === "checkbox") {
            const categoryId = e.target.value;
            const isChecked = e.target.checked;
            const indexDB = window.indexedDB.open("e6eo");
            indexDB.onsuccess = (e) => {
                const db = e.target.result;
                const transaction = db.transaction("categories_checked", "readwrite");
                const objectStore = transaction.objectStore("categories_checked");
                objectStore.put({categoryId, value: isChecked});
            }
            const categoryIndex = categories.findIndex((category) => category[0] === categoryId);
            if (categoryIndex !== -1) {
                const newCategories = [...categories];
                newCategories[categoryIndex][2] = isChecked;
                setCategories(newCategories);
            }
        }
    }

    // 카테고리 편집을 눌렀을 때 입력 가능한 input 창으로 전환되는 함수
    function categoryEditHandler(category) {
        const categoryEl = document.querySelector(`#${CSS.escape(category[0])} + label .categoryName .changeName`);
        if (categoryEl.getAttribute("readonly") !== null) {
            categoryEl.focus();
            categoryEl.removeAttribute("readonly");
            categoryEl.style.setProperty("box-shadow", "-1px -1px 3px 0px inset");
            categoryEl.style.setProperty("background-color", "rgba(255, 255, 255, 0.5)");
            categoryEl.style.setProperty("pointer-events", "auto");
            categoryEl.addEventListener("keydown", (e) => {
                if (e.key === "Enter") {
                    categoryEditHandler(category);
                } else if (e.key === "Escape") {
                    categoryEl.setAttribute("readonly", "true");
                    categoryEl.removeAttribute("style");
                    categoryEl.style.setProperty("pointer-events", "none");
                    categoryEl.value = category[1];
                }
            })
        } else {
            console.log(category[0], categoryEl.value);
            categoryEl.setAttribute("readonly", "true");
            categoryEl.removeAttribute("style");
            categoryEl.style.setProperty("pointer-events", "none");
            if (category[1] !== categoryEl.value) {
                axios.patch("/categories/category", {
                    observe: window.sessionStorage.getItem("observe"),
                    key: category[0],
                    value: categoryEl.value,
                })
            }
        }
    }

    function categoryDeleteHandler(category) {
        if (window.confirm("내부의 노트도 모두 삭제됩니다.\r정말 삭제하시겠습니까?")) {
            axios.delete("/categories/category", {
                data: {
                    observe: window.sessionStorage.getItem("observe"),
                    key: category[0],
                }
            })
        }
    }


    function deleteCategoryListData() {
        const indexDB = window.indexedDB.open("e6eo");
        indexDB.onsuccess = (e) => {
            const db = e.target.result;
            const transaction = db.transaction("categories_checked", "readwrite");
            const objectStore = transaction.objectStore("categories_checked");
            objectStore.clear();
        }
    }


    return (<div className="leftbar">
        <div className="schedule">
            <div className="headLabel">카테고리 리스트
                <div className="iconButton" onClick={toggleCategoryCreate}>
                    <svg xmlns="http://www.w3.org/2000/svg" height="100%" viewBox="0 0 40 40" fill="none"
                         stroke="currentColor" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round"
                         style={{transform: `rotate(${rotationDegree}deg)`}}>{/* 회전 각도 적용 */}
                        <line x1="20" y1="4" x2="20" y2="36"/>
                        <line x1="4" y1="20" x2="36" y2="20"/>
                    </svg>
                </div>
            </div>
            <ul id={"category-list"}>
                {categoryLoading && <p>로딩 중...</p>}
                {!categoryLoading && categories.map((category) => (
                    <div className={"categoryList checkbox-wrapper"} key={category[0]}>
                        <input id={category[0]} value={category[0]} type={"checkbox"}
                               onChange={categoryHandler} defaultChecked={category[2]}/>
                        <label htmlFor={category[0]}>
                            <div className={"checkIcon"}><span></span></div>
                            <div className={"categoryName"}><input className={"changeName"} defaultValue={category[1]}
                                                                   readOnly={true} style={{pointerEvents: "none"}}/>
                            </div>
                        </label>
                        <span className={"iconButtonContainer"}>
                                <span className={"iconButton edit"} onClick={() => categoryEditHandler(category)}>
                                <svg focusable="false" width="20" height="20" viewBox="0 0 24 24">
                                    <path
                                        d="M20.41 4.94l-1.35-1.35c-.78-.78-2.05-.78-2.83 0L3 16.82V21h4.18L20.41 7.77c.79-.78.79-2.05 0-2.83zm-14 14.12L5 19v-1.36l9.82-9.82 1.41 1.41-9.82 9.83z"></path>
                                </svg>
                            </span>
                            <span className={"iconButton delete"} onClick={() => categoryDeleteHandler(category)}>
                                <svg focusable="false" width="20" height="20" viewBox="0 0 24 24">
                                    <path
                                        d="M15 4V3H9v1H4v2h1v13c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V6h1V4h-5zm2 15H7V6h10v13z"/>
                                    <path d="M9 8h2v9H9zm4 0h2v9h-2z"/>
                                </svg>
                            </span>
                        </span>
                    </div>
                ))}
                <div className="category-create"
                     style={{height: isCategoryCreateVisible ? 'auto' : '0px', overflow: 'hidden'}}>
                    <input style={{opacity: isCategoryCreateVisible ? '1' : '0', transition: 'opacity 0.3s ease'}}/>
                    <button style={{
                        opacity: isCategoryCreateVisible ? '1' : '0', transition: 'opacity 0.3s ease'
                    }}>(캘린더)카테고리 추가
                    </button>
                </div>
            </ul>
        </div>
        {/*<div className="today-tasks">*/}
        {/*    <div className="headLabel">오늘 할 일</div>*/}
        {/*</div>*/}
        <div className={"userbar"}>
            <div className={"userProfile"} onClick={handlerUserMenu}>
                <div className={"nickName"}>{userName}</div>
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
    </div>)
}

