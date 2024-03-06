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
    }, );



    const [isSearchVisible, setIsSearchVisible] = useState(false);
    const [calendar, setCalendar] = useState(null);
    const [calendarTitle, setCalendarTitle] = useState(""); // title 상태 추가



    let ymData = {
        year: 0,
        month: 0,
        observe: "",
    };


    function handleToggle() {
        setIsSearchVisible(!isSearchVisible);
    }




    //전달 버튼
    const handlePrevButtonClick = () => {
        if (calendar) {
            calendar.prev();
            setTitle(calendar.view.title);
            const nowDate = calendar.view.currentStart;
            console.log(`현재 년도: ${nowDate.getFullYear()} / ${nowDate.getMonth() + 1}`);
            ymData = ({
                year: calendar.view.currentStart.getFullYear(),
                month: calendar.view.currentStart.getMonth() + 1,
                observe: sessionStorage.getItem("observe")
            });
            console.log(ymData)
        }
    };



    //담달 버튼

    const handleNextButtonClick = () => {
        if (calendar) {
            calendar.next();
            setTitle(calendar.view.title);
            const nowDate = calendar.view.currentStart;
            console.log(`현재 년도: ${nowDate.getFullYear()} / ${nowDate.getMonth() + 1}`);
            ymData = ({
                year: calendar.view.currentStart.getFullYear(),
                month: calendar.view.currentStart.getMonth() + 1,
                observe: sessionStorage.getItem("observe")
            });
            console.log(ymData)
        }
    };


    const handleYMData = async () => {
        const response = await fetch("/notes/ymdata", {
            method: "POST",
            headers: {
                "Content-Type": "application/json; charset=utf-8",
                "Accept": "application/json; charset=utf-8",
            },
            body: JSON.stringify(ymData),
        });
    };


    const handleNextButtonClick1 = () => {
        handleNextButtonClick();
        handleYMData();
    };

    const handlePrevButtonClick1 = () => {
        handlePrevButtonClick();
        handleYMData();
    }


    const setTitle = (title) => {
        setCalendarTitle(title);
    };

    const handleAddEventButtonClick = () => {
        // Handle add event button click functionality
    };

    return (
        <div className={"Main"}>
            <MyContext.Provider value={{isSearchVisible, handleToggle}}>
                <div className={"3dan"}>
                    <Header
                        onPrevButtonClick={handlePrevButtonClick1}
                        onNextButtonClick={handleNextButtonClick1}
                        currentTitle={calendarTitle} /* 수정: currentTitle prop 전달 */
                    />
                    <div className={"leftOUT"}>
                        <LeftBar/>
                        {isSearchVisible ? <Search/> :
                            <Center setMainCalendar={setCalendar} setTitle={setTitle}/>} {/* setTitle prop 추가 */}
                    </div>
                </div>
            </MyContext.Provider>
        </div>
    );
}
