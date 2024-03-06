import React, {useEffect, useState} from "react";
import Header from "./Header";
import LeftBar from "./LeftBar";
import Search from "./Search";
import Center from "./Center";
import {useNavigate} from "react-router-dom";

export const MyContext = React.createContext();

export default function Main() {
    const redirect = useNavigate()

    useEffect(() => {
        // alert(window.sessionStorage.getItem("observe"));
        if (window.sessionStorage.getItem("observe") == null) {
            redirect("/");
        }
    }, []);

    const [isSearchVisible, setIsSearchVisible] = useState(false);
    const [calendar, setCalendar] = useState(null);
    const [calendarTitle, setCalendarTitle] = useState(""); // title 상태 추가

    function handleToggle() {
        setIsSearchVisible(!isSearchVisible);
    }

    //전달 버튼
    const handlePrevButtonClick = () => {
        if (calendar) {
            calendar.prev();
            setTitle(calendar.view.title);
        }
    };

    //담달 버튼
    const handleNextButtonClick = () => {
        if (calendar) {
            calendar.next();
            setTitle(calendar.view.title);
        }
    };

    const setTitle = (title) => {
        setCalendarTitle(title);
    };

    const handleAddEventButtonClick = () => {
        // Handle add event button click functionality
    };

    return (
        <div className={"Main"}>
            <MyContext.Provider value={{ isSearchVisible, handleToggle }}>
                <div className={"3dan"}>
                    <Header
                        onPrevButtonClick={handlePrevButtonClick}
                        onNextButtonClick={handleNextButtonClick}
                        currentTitle={calendarTitle} /* 수정: currentTitle prop 전달 */
                    />
                    <div className={"leftOUT"}>
                        <LeftBar />
                        {isSearchVisible ? <Search /> : <Center setMainCalendar={setCalendar} setTitle={setTitle} />} {/* setTitle prop 추가 */}
                    </div>
                </div>
            </MyContext.Provider>
        </div>
    );
}
