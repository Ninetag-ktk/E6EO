import React, {useEffect, useState} from 'react';
import axios from "axios";
import {deleteNote, insertNote, patchNote} from "./googleCRUD";

export default function ({noteRef, categories, closeModal}) {
    const [noteCategory, setNoteCategory] = useState('');
    const [noteType, setNoteType] = useState('memo');
    const [infoToggle, setInfoToggle] = useState(true);
    const [allDay, setAllDay] = useState(false);
    const [note, setNote] = useState({
        id: "",
        categoryId: "",
        type: "",
        startTime: "",
        endTime: "",
        etag: "",
        title: "",
        contents: "",
        status: "",
        haveRepost: "",
    })

    // console.log(noteRef)
    function settingNote(noteRef) {
        if (!noteRef.startTime.includes("T")) {
            setAllDay(true);
        }
        setNote({
            id: noteRef.id,
            categoryId: noteRef.categoryId.split("#")[1] ? noteRef.categoryId.split("#")[1].replaceAll("_", ".") : noteRef.categoryId,
            type: noteRef.type,
            startTime: datetimeCheck(noteRef.startTime),
            endTime: datetimeCheck(noteRef.endTime),
            etag: noteRef.etag,
            title: noteRef.title,
            contents: noteRef.contents,
            status: noteRef.status,
            haveRepost: noteRef.haveRepost,
        })
    }

    function settingDefaultNote() {
        setNote({
            ...note,
            id: null,
            categoryId: "e6eo",
            type: "memo",
            startTime: new Date(Date.now() - (new Date().getTimezoneOffset() * 60000)).toISOString().slice(0, -8) + ":00.000Z",
            endTime: new Date(Date.now() - (new Date().getTimezoneOffset() * 60000)).toISOString().slice(0, -8) + ":00.000Z",
            status: "confirm",
            haveRepost: null,
        })
    }

    function datetimeCheck(date, allDayState) {
        if (allDayState) {
            return date.slice(0, 10);
        } else {
            if (date.endsWith(":00.000Z")) {
                return date;
            } else if (date.endsWith("+09:00")) {
                return date.replace("+09:00", ".000Z");
            } else if (date.includes("T")) {
                return date + ":00.000Z";
            } else {
                return date + "T00:00:00.000Z";
            }
        }
    }

    const onChangeNote = (e) => {
        const {value, name} = e.target;
        setNote({
            ...note,
            [name]: value,
        })
    }

    const onChangeNoteCategotyAndType = (e) => {
        const {value, name} = e.target;
        if (e.target.value.startsWith("google")) {
            const noteCategoryType = e.target.value.split("^");
            switch (noteCategoryType[1]) {
                case "calendar":
                    setNoteType("event");
                    setNote({
                        ...note,
                        [name]: value,
                        type: "event",
                    });
                    break;
                case "tasks":
                    setNoteType("task");
                    setNote({
                        ...note,
                        [name]: value,
                        type: "task",
                    });
                    break;
                default:
                    setNoteType("memo")
                    setNote({
                        ...note,
                        [name]: value,
                        type: "memo",
                    });
            }
        } else {
            setNoteType("memo");
            setNote({
                ...note,
                [name]: value,
                type: "memo",
            });
        }
    }

    const onChangeNoteDateTime = (e) => {
        const {value, name} = e.target;
        const time = note[name];
        // console.log(name, value)
        if (allDay) {
            setNote({
                ...note,
                [name]: value,
            })
        } else {
            setNote({
                ...note,
                [name]: value + time.slice(value.length, time.length),
            })
        }
    }
    const allDayChange = (e) => {
        setAllDay(e.target.checked)
        setNote({
            ...note,
            startTime: datetimeCheck(note.startTime, e.target.checked),
            endTime: datetimeCheck(note.endTime, e.target.checked),
        })
    }


    useEffect(() => {
        if (noteRef) {
            settingNote(noteRef);
        } else {
            settingDefaultNote();
        }
    }, [])

    useEffect(() => {
        setNoteCategory(note.categoryId);
        setNoteType(note.type);
        console.log("수정 체크 : ", note);
    }, [note])

    useEffect(() => {
        typeFilter();
    }, [noteCategory])

    useEffect(() => {
        interfaceByType();
    }, [noteType, allDay])

    useEffect(() => {
        if (noteRef) {
            notePromise();
            // console.log(infoToggle)
        }
    }, [infoToggle])

    const typeHandle = (e) => {
        // console.log(e.target.value);
        setNoteType(e.target.value);
    };

    const infoModeHandle = () => {
        setInfoToggle(!infoToggle);
    }

    const handleResizeHeight = (e) => {
        e.target.style.height = 'fit-contents'; //height 초기화
        e.target.style.height = e.target.scrollHeight + 'px';
    };

    function notePromise() {

        function noteTypeIcon() {
            switch (note.type) {
                case "event": {
                    return (
                        <svg className={"svgIcon"} xmlns="http://www.w3.org/2000/svg" viewBox="60 60 400 400"
                             id="calendar">
                            <path
                                d="M368.005 272h-96v96h96v-96zm-32-208v32h-160V64h-48v32h-24.01c-22.002 0-40 17.998-40 40v272c0 22.002 17.998 40 40 40h304.01c22.002 0 40-17.998 40-40V136c0-22.002-17.998-40-40-40h-24V64h-48zm72 344h-304.01V196h304.01v212z"/>
                        </svg>
                    )
                    break;
                }
                case "task": {
                    return (
                        <svg className={"svgIcon"} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"
                             id="time">
                            <path
                                d="M12.004.998C5.94.998.996 5.935.996 11.998s4.945 11.006 11.008 11.006c6.063 0 10.998-4.943 10.998-11.006 0-6.063-4.935-11-10.998-11zm0 2a8.983 8.983 0 0 1 8.998 9 8.988 8.988 0 0 1-8.998 9.006 8.997 8.997 0 0 1-9.008-9.006c0-4.982 4.026-9 9.008-9zm-.016 1.986a1 1 0 0 0-.99 1.02v5.994a1 1 0 0 0 .297.707l4 4.002a1 1 0 1 0 1.41-1.418L13 11.584v-5.58a1 1 0 0 0-1.012-1.02z"/>
                        </svg>
                    )
                    break;
                }
                case "memo": {
                    return (
                        <svg className={"svgIcon"} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 80 78" id="notes">
                            <path
                                d="M75.8 11.6a1.985 1.985 0 0 0-2.6 3 7.392 7.392 0 0 1 2.8 5.8v46.4a7.514 7.514 0 0 1-7.4 7.4H11.4A7.514 7.514 0 0 1 4 66.8V20.4a7.514 7.514 0 0 1 7.4-7.4h30a2 2 0 1 0 0-4h-30A11.27 11.27 0 0 0 0 20.4v46.2C.068 72.868 5.132 77.932 11.4 78h57A11.4 11.4 0 0 0 80 66.8V20.4a11.606 11.606 0 0 0-4.2-8.8zM56.2 68a2.3 2.3 0 0 0 1.8 1.2 1.922 1.922 0 0 0 1.8-1.2l6-12.2a1.2 1.2 0 0 0 .2-.8V8a8 8 0 1 0-16 0v47a.965.965 0 0 0 .2.8l6 12.2zm1.8-5.4L55.2 57h5.6L58 62.6zM54 8a4 4 0 1 1 8 0v17.2h-8V8zm0 21.2h8v4h-8v-4zm0 8.2h8V53h-8V37.4zm-14.2-5H12.2a2 2 0 1 0 0 4H40a1.847 1.847 0 0 0 1.8-2 1.89 1.89 0 0 0-2-2zm0 8H12.2a2 2 0 1 0 0 4H40a1.847 1.847 0 0 0 1.8-2 1.89 1.89 0 0 0-2-2zm0 8H12.2a2 2 0 1 0 0 4H40a1.847 1.847 0 0 0 1.8-2 1.89 1.89 0 0 0-2-2z"/>
                        </svg>
                    )
                    break;
                }
                default:
                    return null;
            }
        }

        function noteDate() {
            const today = new Date();

            function parseStart() {
                const [date, time] = note.startTime.split('T');
                const dateObject = new Date(date);
                const [year, month, day] = date.split('-');
                const weekday = dateObject.toLocaleDateString('ko-KR', {
                    weekday: 'long',
                });
                if (time === undefined || time.endsWith('00:00:00.000Z')) {
                    if (year === today.getFullYear().toString()) {
                        return `${month}월 ${day}일 (${weekday})`;
                    } else {
                        return `${year}년 ${month}월 ${day}일 (${weekday})`;
                    }
                } else {
                    const [hours, minutes, seconds] = time.split(':');
                    const hour12 = hours % 12 || 12;
                    const ampm = hours >= 12 ? '오후' : '오전';
                    if (year === today.getFullYear().toString()) {
                        return `${month}월 ${day}일 (${weekday}) ${hours}시${minutes}분`;
                    } else {
                        return `${year}년 ${month}월 ${day}일 (${weekday})`;
                    }
                }
            }

            function parseEnd() {
                const [date, time] = note.endTime.split('T');
                const dateObject = new Date(date);
                const [year, month, day] = date.split('-');
                const weekday = dateObject.toLocaleDateString('ko-KR', {
                    weekday: 'long',
                });
                if (time === undefined || time.endsWith('00:00:00.000Z')) {
                    if (year === today.getFullYear().toString()) {
                        return `${month}월 ${day}일 (${weekday})`;
                    } else {
                        return `${year}년 ${month}월 ${day}일 (${weekday})`;
                    }
                } else {
                    const [hours, minutes, seconds] = time.split(':');
                    const hour12 = hours % 12 || 12;
                    const ampm = hours >= 12 ? '오후' : '오전';
                    if (year === today.getFullYear().toString()) {
                        return `${month}월 ${day}일 (${weekday}) ${hours}시${minutes}분`;
                    } else {
                        return `${year}년 ${month}월 ${day}일 (${weekday})`;
                    }
                }
            }

            function varCheck() {
                const start = parseStart();
                const end = parseEnd();
                let diffIndex = -1;
                for (let i = 0; i < end.length; i++) {
                    if (start[i] !== end[i]) {
                        diffIndex = i;
                        break;
                    }
                }
                if (diffIndex === -1) {
                    return `${start}`;
                } else {
                    return `${start} - ${end.slice(diffIndex - 1, end.length)}`;
                }
            }

            return varCheck();
        }

        function infoMode() {
            return (
                <div className={"noteOuter"}>
                    <div className={"noteTitleOuter"}>
                        <span className={"noteIconContainer"}><span/></span>
                        <span className={"noteTitle"}>{note.title}</span>
                    </div>
                    <div className={"noteBody"}>
                        <div className={"noteDue noteBody-info-outer"}>
                                <span className={"noteIconContainer"}>
                                    {noteTypeIcon()}
                                </span>
                            <span className={"noteBody-info"}>
                                    {noteDate()}
                                </span>
                        </div>
                        {note.contents !== null &&
                            <div className={"noteContents noteBody-info-outer"}>
                                    <span className={"noteIconContainer"}>
                                        <svg className={"svgIcon"} xmlns="http://www.w3.org/2000/svg"
                                             viewBox="0 0 32 32" id="comment"><path
                                            d="M25,2H7A6,6,0,0,0,1,8V18a6,6,0,0,0,6,6h8.68l9.74,6.82a1,1,0,0,0,1.56-1l-.85-6A6,6,0,0,0,31,18V8A6,6,0,0,0,25,2Zm4,16a4,4,0,0,1-4,4,1,1,0,0,0-1,1.14l.67,4.72-8.11-5.68A1,1,0,0,0,16,22H7a4,4,0,0,1-4-4V8A4,4,0,0,1,7,4H25a4,4,0,0,1,4,4Z"></path><path
                                            d="M25 7H12a1 1 0 0 0 0 2H25a1 1 0 0 0 0-2zM25 12H7a1 1 0 0 0 0 2H25a1 1 0 0 0 0-2zM25 17H7a1 1 0 0 0 0 2H25a1 1 0 0 0 0-2z"/></svg>
                                    </span>
                                <span className={"noteBody-info"}>
                                        {note.contents}
                                    </span>
                            </div>}
                    </div>
                    <div className={"noteFooter"}>
                    </div>
                </div>
            )
        }

        function editMode() {
            const options = categories.map((category) => {
                if (note.categoryId.includes("google") && note.categoryId.includes("calendar")) {
                    if (category[0].includes("calendar")) {
                        return <option value={category[0]}
                                       selected={note.categoryId.endsWith(category[0].replaceAll("_", ".")) ? true : false}>{category[1]}</option>
                    }
                } else if (note.categoryId.includes("google") && note.categoryId.includes("tasks")) {
                    if (category[0].includes("tasks")) {
                        return <option value={category[0]}
                                       selected={note.categoryId.endsWith(category[0].replaceAll("_", ".")) ? true : false}>{category[1]}</option>
                    }
                } else {
                    if (!category[0].includes("google")) {
                        return <option value={category[0]}
                                       selected={note.categoryId.endsWith(category[0].replaceAll("_", ".")) ? true : false}>{category[1]}</option>
                    }
                }
            })

            return (
                <div className={"noteOuter"}>
                    <div className={"noteTitleOuter"}>
                        <select className={"categorySelect"} name={"categoryId"}
                                onChange={(e) => {
                                    setNoteCategory(e.target.value);
                                    onChangeNoteCategotyAndType(e);
                                }}>{options}</select>
                        <textarea className={"noteTitle"} name={"title"} value={noteRef ? note.title : null}
                                  onChange={(e) => {
                                      handleResizeHeight(e);
                                      onChangeNote(e);
                                  }}
                                  rows={1}/>
                    </div>
                    {typeFilter()}
                    {interfaceByType()}
                    <div className={"noteFooter"}>
                        <button onClick={patchNoteHandler}>저장</button>
                    </div>
                </div>
            )
        }

        return (
            <div className={"noteContainer"}>
                <div className={"noteHeader"}>
                    <span className={"noteMenuContainer"}>
                        <span className={"iconButton"}>
                            <svg className={"svgIcon"} xmlns="http://www.w3.org/2000/svg" viewBox="0 -40 512 512"
                                 id="conversation">
                                <path d="M457.387,132.5h-37.982V74.544c0-29.95-24.366-54.316-54.316-54.316H58.316C28.366,20.228,4,44.594,4,74.544V279.21
	c0,29.95,24.366,54.316,54.316,54.316h30.127v61.122c0,5.03,3.137,9.526,7.858,11.263c1.353,0.497,2.753,0.738,4.139,0.738
	c3.453,0,6.819-1.493,9.144-4.228l57.777-67.94v35.386c0,27.908,22.705,50.613,50.613,50.613h112.835l58.596,67.179
	c2.325,2.665,5.645,4.113,9.046,4.113c1.411,0,2.836-0.25,4.209-0.765c4.685-1.756,7.788-6.233,7.788-11.236v-59.291h46.937
	c27.908,0,50.613-22.705,50.613-50.613V183.113C508,155.205,485.295,132.5,457.387,132.5z M153.485,313.752l-41.042,48.262v-40.487
	c0-6.628-5.373-12-12-12H58.316C41.6,309.526,28,295.927,28,279.21V74.544c0-16.717,13.6-30.316,30.316-30.316h306.772
	c16.716,0,30.316,13.6,30.316,30.316V279.21c0,16.717-13.6,30.316-30.316,30.316H162.627
	C159.107,309.526,155.766,311.071,153.485,313.752z M484,369.868c0,14.675-11.939,26.613-26.613,26.613H398.45
	c-6.627,0-12,5.373-12,12v39.277l-41.14-47.165c-2.279-2.613-5.577-4.112-9.043-4.112H217.976
	c-14.674,0-26.613-11.938-26.613-26.613v-36.342h173.726c29.95,0,54.316-24.366,54.316-54.316V156.5h37.982
	c14.674,0,26.613,11.938,26.613,26.613V369.868z M230.756,176.877c0,10.523-8.531,19.054-19.053,19.054
	c-10.523,0-19.053-8.531-19.053-19.054c0-10.522,8.53-19.053,19.053-19.053C222.225,157.824,230.756,166.354,230.756,176.877z
	 M128.367,176.877c0,10.523-8.531,19.054-19.053,19.054c-10.523,0-19.053-8.531-19.053-19.054c0-10.522,8.53-19.053,19.053-19.053
	C119.836,157.824,128.367,166.354,128.367,176.877z M333.144,176.877c0,10.523-8.531,19.054-19.054,19.054
	c-10.523,0-19.053-8.531-19.053-19.054c0-10.522,8.53-19.053,19.053-19.053C324.613,157.824,333.144,166.354,333.144,176.877z"/></svg>
                        </span>
                        <span className={"iconButton"} onClick={infoModeHandle}>
<svg className={"svgIcon"} xmlns="http://www.w3.org/2000/svg" viewBox="-4 -2 36 36" id="edit"><path
    d="M26.857,31.89H3c-1.654,0-3-1.346-3-3V5.032c0-1.654,1.346-3,3-3h16.214c0.553,0,1,0.448,1,1s-0.447,1-1,1H3c-0.551,0-1,0.449-1,1V28.89c0,0.551,0.449,1,1,1h23.857c0.552,0,1-0.449,1-1V12.675c0-0.552,0.447-1,1-1s1,0.448,1,1V28.89C29.857,30.544,28.512,31.89,26.857,31.89z M24.482,23.496c-0.002,0-0.003,0-0.005,0L5.192,23.407c-0.553-0.002-0.998-0.452-0.996-1.004c0.002-0.551,0.45-0.996,1-0.996c0.001,0,0.003,0,0.004,0l19.286,0.089c0.552,0.002,0.998,0.452,0.995,1.004C25.479,23.051,25.032,23.496,24.482,23.496z M15.251,18.415c-0.471,0-0.781-0.2-0.957-0.366c-0.297-0.28-0.709-0.931-0.14-2.151l0.63-1.35c0.516-1.104,1.596-2.646,2.459-3.51L26,2.281c0.003-0.002,0.005-0.004,0.007-0.006c0.002-0.002,0.004-0.004,0.006-0.006l0.451-0.451c1.168-1.169,2.979-1.262,4.036-0.207c0,0,0,0,0,0c1.056,1.055,0.963,2.866-0.207,4.036c0,0-0.536,0.552-0.586,0.586l-8.635,8.635c-0.85,0.85-2.345,1.964-3.405,2.538l-1.218,0.657C15.969,18.322,15.572,18.415,15.251,18.415z M26.714,4.396l-8.056,8.057c-0.699,0.7-1.644,2.047-2.061,2.942L16.4,15.815l0.316-0.17c0.885-0.479,2.233-1.482,2.942-2.192l8.057-8.057L26.714,4.396z M28.163,3.016l0.932,0.932c0.2-0.356,0.177-0.737-0.009-0.923C28.881,2.82,28.499,2.83,28.163,3.016z"/></svg>
                        </span>
                        <span class={"iconButton"} onClick={deleteNoteHandler}>
                                <svg className={"svgIcon"} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32"
                                     id="trash">
                                    <path
                                        d="M3 9h2v15.46C5 27.58 8.52 30 11.54 30h8.92c3 0 6.54-2.42 6.54-5.54V9h2a1 1 0 0 0 0-2h-5.07c-.34-2.47-2.11-5-7.93-5S8.41 4.53 8.07 7H3a1 1 0 0 0 0 2Zm13-5c4.38 0 5.6 1.51 5.91 3H10.09c.31-1.49 1.53-3 5.91-3Zm9 5v15.46c0 1.87-2.54 3.54-4.54 3.54h-8.92C9.54 28 7 26.33 7 24.46V9Z"/>
                                    <path
                                        d="M16 26a1 1 0 0 0 1-1V12a1 1 0 0 0-2 0v13a1 1 0 0 0 1 1zm-5 0a1 1 0 0 0 1-1V12a1 1 0 0 0-2 0v13a1 1 0 0 0 1 1zm10 0a1 1 0 0 0 1-1V12a1 1 0 0 0-2 0v13a1 1 0 0 0 1 1z"/></svg>
                        </span>
                    </span>
                    <span className={"noteCancel iconButton"} onClick={closeModal}>
                        <svg className={"svgIcon"} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" id="cancel"><path
                            d="M13.41,12l4.3-4.29a1,1,0,1,0-1.42-1.42L12,10.59,7.71,6.29A1,1,0,0,0,6.29,7.71L10.59,12l-4.3,4.29a1,1,0,0,0,0,1.42,1,1,0,0,0,1.42,0L12,13.41l4.29,4.3a1,1,0,0,0,1.42,0,1,1,0,0,0,0-1.42Z"></path></svg>
                    </span>
                </div>
                {infoToggle ? infoMode() : editMode()}
            </div>
        )
    }

    function noteEmpty() {
        const options = categories.map((category) => {
            return <option value={category[0]}>{category[1]}</option>
        })

        return (<div className={"noteContainer"}>
            <div className={"noteHeader"}>
                <span className={"noteCancel iconButton"} onClick={closeModal}>
                    <svg className={"svgIcon"} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" id="cancel"><path
                        d="M13.41,12l4.3-4.29a1,1,0,1,0-1.42-1.42L12,10.59,7.71,6.29A1,1,0,0,0,6.29,7.71L10.59,12l-4.3,4.29a1,1,0,0,0,0,1.42,1,1,0,0,0,1.42,0L12,13.41l4.29,4.3a1,1,0,0,0,1.42,0,1,1,0,0,0,0-1.42Z"></path></svg>
                </span>
            </div>
            <div className={"noteOuter"}>
                <div className={"noteTitleOuter"}>
                    <select className={"categorySelect"} name={"categoryId"}
                            onChange={(e) => {
                                // console.log("확인", e.target.name, e.target.value);
                                setNoteCategory(e.target.value);
                                onChangeNoteCategotyAndType(e);
                            }}>{options}</select>
                    <textarea className={"noteTitle"} name={"title"} placeholder={"제목 및 시간 추가"}
                              onChange={(e) => {
                                  handleResizeHeight(e);
                                  onChangeNote(e);
                              }}
                              rows={1}/>
                </div>
                {typeFilter()}
                {interfaceByType()}
                <div className={"noteFooter"}>
                    <button onClick={insertNoteHandler}>저장</button>
                </div>
            </div>
        </div>)
    }

    function typeFilter() {
        function eventRadio() {
            return (
                <div className={"typeSelect"}>
                    <input type={"radio"} name={"type"} value={"event"} id={"eventType"}
                           checked={noteType === "event"} className={"typeSelect-input"} onChange={(e) => {
                        onChangeNote(e);
                        typeHandle(e);
                    }}/>
                    <label htmlFor={"eventType"} className={"typeSelect-label"}>
                        <svg className={"svgIcon"} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16"
                             id="event">
                            <path
                                d="M5 4.5a.5.5 0 0 1-.5-.5V2a.5.5 0 0 1 1 0v2a.5.5 0 0 1-.5.5zM11 4.5a.5.5 0 0 1-.5-.5V2a.5.5 0 0 1 1 0v2a.5.5 0 0 1-.5.5z"/>
                            <path
                                d="M13 14.5H3c-.827 0-1.5-.673-1.5-1.5V4c0-.827.673-1.5 1.5-1.5h10c.827 0 1.5.673 1.5 1.5v9c0 .827-.673 1.5-1.5 1.5zM3 3.5a.5.5 0 0 0-.5.5v9a.5.5 0 0 0 .5.5h10a.5.5 0 0 0 .5-.5V4a.5.5 0 0 0-.5-.5H3z"/>
                            <path
                                d="M14 6.5H2a.5.5 0 0 1 0-1h12a.5.5 0 0 1 0 1zM5.5 7.5h1v1h-1zM7.5 7.5h1v1h-1zM9.5 7.5h1v1h-1zM11.5 7.5h1v1h-1zM3.5 9.5h1v1h-1zM5.5 9.5h1v1h-1zM7.5 9.5h1v1h-1zM9.5 9.5h1v1h-1zM11.5 9.5h1v1h-1zM3.5 11.5h1v1h-1zM5.5 11.5h1v1h-1zM7.5 11.5h1v1h-1z"/>
                        </svg>
                        <span>event</span>
                    </label>
                </div>
            )
        }

        function taskRadio() {
            return (
                <div className={"typeSelect"}>
                    <input type={"radio"} name={"type"} value={"task"} id={"taskType"}
                           checked={noteType === "task"} className={"typeSelect-input"} onChange={(e) => {
                        onChangeNote(e);
                        typeHandle(e);
                    }}/>
                    <label htmlFor={"taskType"} className={"typeSelect-label"}>
                        <svg className={"svgIcon"} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" id="task">
                            <path
                                d="M20.4,12.349a.5.5,0,0,1-.5.5H11.288a.5.5,0,0,1,0-1H19.9A.5.5,0,0,1,20.4,12.349ZM11.288,6.8H19.9a.5.5,0,0,0,0-1H11.288a.5.5,0,0,0,0,1ZM19.9,17.9H11.288a.5.5,0,0,0,0,1H19.9a.5.5,0,0,0,0-1ZM6.85,10.1a2.25,2.25,0,1,0,2.25,2.25A2.253,2.253,0,0,0,6.85,10.1Zm1.54-5.57L6.34,6.949l-.84-1.1a.5.5,0,0,0-.7-.1.507.507,0,0,0-.1.7l1.22,1.6a.517.517,0,0,0,.39.2h.01a.527.527,0,0,0,.38-.18l2.45-2.9a.5.5,0,0,0-.76-.64ZM6.85,16.149A2.25,2.25,0,1,0,9.1,18.4,2.253,2.253,0,0,0,6.85,16.149Z"></path>
                        </svg>
                        <span>task</span>
                    </label>
                </div>
            )
        }

        function memoRadio() {
            return (
                <div className={"typeSelect"}>
                    <input type={"radio"} name={"type"} value={"memo"} id={"memoType"}
                           checked={noteType === "memo"} className={"typeSelect-input"} onChange={(e) => {
                        onChangeNote(e);
                        typeHandle(e);
                    }}/>
                    <label htmlFor={"memoType"} className={"typeSelect-label"}>
                        <svg className={"svgIcon"} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" id="memo">
                            <g data-name="2">
                                <path
                                    d="M25.75,10a.6.6,0,0,0-.07-.3s0,0,0-.07a.79.79,0,0,0-.14-.18v0l-7-6-.12-.07-.09-.06A.87.87,0,0,0,18,3.26H7A.75.75,0,0,0,6.25,4V28a.75.75,0,0,0,.75.75H25a.76.76,0,0,0,.75-.75V10Zm-7-4.38L23,9.27H18.75Zm-11,21.6V4.76h9.5V10a.77.77,0,0,0,.22.53.75.75,0,0,0,.53.22h6.25V27.24Z"></path>
                                <path
                                    d="M10.06 9.83h3.83a.75.75 0 0 0 0-1.5H10.06a.75.75 0 0 0 0 1.5zM21.94 12.31H10.06a.75.75 0 0 0 0 1.5H21.94a.75.75 0 0 0 0-1.5zM21.94 16.26H10.06a.75.75 0 1 0 0 1.5H21.94a.75.75 0 0 0 0-1.5zM21.94 20.23H10.06a.75.75 0 1 0 0 1.5H21.94a.75.75 0 0 0 0-1.5zM21.94 24H10.06a.75.75 0 1 0 0 1.5H21.94a.75.75 0 0 0 0-1.5z"></path>
                            </g>
                        </svg>
                        <span>memo</span>
                    </label>
                </div>
            )
        }

        if (noteCategory.startsWith("google")) {
            console.log(noteCategory)
            const noteCategoryType = noteCategory.split("\^");
            console.log(noteCategoryType[1])
            switch (noteCategoryType[1]) {
                case "calendar": {
                    return <div className={"typeContainer"}> {eventRadio()} </div>
                }
                case "tasks": {
                    return <div className={"typeContainer"}> {taskRadio()} </div>
                }
                default: {
                    return null;
                }
            }
        } else {
            return <div className={"typeContainer"}>
                {eventRadio()}
                {taskRadio()}
                {memoRadio()}
            </div>
        }
    }

    function interfaceByType() {
        function eventInterface() {
            function allDayEvent() {
                return (<span>
                    <input type={"date"} name={"startTime"} onChange={onChangeNoteDateTime}
                           value={noteRef ? datetimeCheck(note.startTime).slice(0, 10) : datetimeCheck(note.startTime).slice(0, 10)}
                    /> &nbsp;&nbsp;-&nbsp;&nbsp;<input
                    type={"date"} name={"endTime"} onChange={onChangeNoteDateTime}
                    value={noteRef ? datetimeCheck(note.endTime).slice(0, 10) : datetimeCheck(note.endTime).slice(0, 10)}
                /></span>)
            }

            function notAllDayEvent() {
                return (<span>
                    <input type={"datetime-local"} name={"startTime"} onChange={onChangeNoteDateTime}
                           value={noteRef ? datetimeCheck(note.startTime).slice(0, -8) : datetimeCheck(note.startTime).slice(0, -8)}
                    /> &nbsp;&nbsp;-&nbsp;&nbsp;<input
                    type={"datetime-local"} name={"endTime"} onChange={onChangeNoteDateTime}
                    value={noteRef ? datetimeCheck(note.endTime).slice(0, -8) : datetimeCheck(note.endTime).slice(0, -8)}
                /></span>)
            }

            return (<div className={"noteBody"}>
                    <div className={"noteBody-info-outer"}>
                        <span className={"noteIconContainer"}>
                        <svg className={"svgIcon"} xmlns="http://www.w3.org/2000/svg" viewBox="60 60 400 400"
                             id="calendar">
                            <path
                                d="M368.005 272h-96v96h96v-96zm-32-208v32h-160V64h-48v32h-24.01c-22.002 0-40 17.998-40 40v272c0 22.002 17.998 40 40 40h304.01c22.002 0 40-17.998 40-40V136c0-22.002-17.998-40-40-40h-24V64h-48zm72 344h-304.01V196h304.01v212z"/>
                        </svg>
                        </span>
                        <span className={"noteBody-info"}>
                            {allDay ? allDayEvent() : notAllDayEvent()}
                            <span className={"allDayEventToggle"}><input type={"checkbox"} checked={allDay}
                                                                         onChange={(e) => {
                                                                             allDayChange(e);
                                                                         }}/>종일 일정</span>
                         </span>
                    </div>
                    <div className={"noteBody-info-outer noteContents"}>
                        <span className={"noteIconContainer"}>
                                        <svg className={"svgIcon"} xmlns="http://www.w3.org/2000/svg"
                                             viewBox="0 0 32 32" id="comment"><path
                                            d="M25,2H7A6,6,0,0,0,1,8V18a6,6,0,0,0,6,6h8.68l9.74,6.82a1,1,0,0,0,1.56-1l-.85-6A6,6,0,0,0,31,18V8A6,6,0,0,0,25,2Zm4,16a4,4,0,0,1-4,4,1,1,0,0,0-1,1.14l.67,4.72-8.11-5.68A1,1,0,0,0,16,22H7a4,4,0,0,1-4-4V8A4,4,0,0,1,7,4H25a4,4,0,0,1,4,4Z"></path><path
                                            d="M25 7H12a1 1 0 0 0 0 2H25a1 1 0 0 0 0-2zM25 12H7a1 1 0 0 0 0 2H25a1 1 0 0 0 0-2zM25 17H7a1 1 0 0 0 0 2H25a1 1 0 0 0 0-2z"/></svg>
                                    </span>
                        <span className={"noteBody-info"}>
                            <textarea onChange={(e) => {
                                onChangeNote(e);
                                handleResizeHeight(e);
                            }} rows={1} name={"contents"}
                                      value={note.contents !== '' ? note.contents : ''}/>
                            </span>
                    </div>
                </div>
            )
        }

        function taskInterface() {
            return (<div className={"noteBody"}>
                <div className={"noteBody-info-outer"}>
                        <span className={"noteIconContainer"}>
                        <svg className={"svgIcon"} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"
                             id="time">
                            <path
                                d="M12.004.998C5.94.998.996 5.935.996 11.998s4.945 11.006 11.008 11.006c6.063 0 10.998-4.943 10.998-11.006 0-6.063-4.935-11-10.998-11zm0 2a8.983 8.983 0 0 1 8.998 9 8.988 8.988 0 0 1-8.998 9.006 8.997 8.997 0 0 1-9.008-9.006c0-4.982 4.026-9 9.008-9zm-.016 1.986a1 1 0 0 0-.99 1.02v5.994a1 1 0 0 0 .297.707l4 4.002a1 1 0 1 0 1.41-1.418L13 11.584v-5.58a1 1 0 0 0-1.012-1.02z"/>
                        </svg>
                        </span>
                    <span className={"noteBody-info"}>
                        <input type={"date"} name={"startTime"} onChange={onChangeNoteDateTime}
                               value={noteRef ? note.startTime.slice(0, -14) : datetimeCheck(note.startTime).slice(0, -14)}/>
                        </span>
                </div>
                <div className={"noteBody-info-outer noteContents"}>
                        <span className={"noteIconContainer"}>
                                        <svg className={"svgIcon"} xmlns="http://www.w3.org/2000/svg"
                                             viewBox="0 0 32 32" id="comment"><path
                                            d="M25,2H7A6,6,0,0,0,1,8V18a6,6,0,0,0,6,6h8.68l9.74,6.82a1,1,0,0,0,1.56-1l-.85-6A6,6,0,0,0,31,18V8A6,6,0,0,0,25,2Zm4,16a4,4,0,0,1-4,4,1,1,0,0,0-1,1.14l.67,4.72-8.11-5.68A1,1,0,0,0,16,22H7a4,4,0,0,1-4-4V8A4,4,0,0,1,7,4H25a4,4,0,0,1,4,4Z"></path><path
                                            d="M25 7H12a1 1 0 0 0 0 2H25a1 1 0 0 0 0-2zM25 12H7a1 1 0 0 0 0 2H25a1 1 0 0 0 0-2zM25 17H7a1 1 0 0 0 0 2H25a1 1 0 0 0 0-2z"/></svg>
                                    </span>
                    <span className={"noteBody-info"}>
                        <textarea onChange={(e) => {
                            onChangeNote(e);
                            handleResizeHeight(e);
                        }} rows={1} name={"contents"}
                                  value={note.contents !== '' ? note.contents : ''}/>
                        </span>
                </div>
            </div>)
        }

        function memoInterface() {
            return (<div className={"noteBody"}>
                    <div className={"noteBody-info-outer noteContents"}>
                        <span className={"noteIconContainer"}>
                                        <svg className={"svgIcon"} xmlns="http://www.w3.org/2000/svg"
                                             viewBox="0 0 32 32" id="comment"><path
                                            d="M25,2H7A6,6,0,0,0,1,8V18a6,6,0,0,0,6,6h8.68l9.74,6.82a1,1,0,0,0,1.56-1l-.85-6A6,6,0,0,0,31,18V8A6,6,0,0,0,25,2Zm4,16a4,4,0,0,1-4,4,1,1,0,0,0-1,1.14l.67,4.72-8.11-5.68A1,1,0,0,0,16,22H7a4,4,0,0,1-4-4V8A4,4,0,0,1,7,4H25a4,4,0,0,1,4,4Z"></path><path
                                            d="M25 7H12a1 1 0 0 0 0 2H25a1 1 0 0 0 0-2zM25 12H7a1 1 0 0 0 0 2H25a1 1 0 0 0 0-2zM25 17H7a1 1 0 0 0 0 2H25a1 1 0 0 0 0-2z"/></svg>
                                    </span>
                        <span className={"noteBody-info"}>
                            <textarea onChange={(e) => {
                                onChangeNote(e);
                                handleResizeHeight(e);
                            }} rows={1} name={"contents"}
                                      value={note.contents !== '' ? note.contents : ''}/>
                        </span>
                    </div>
                </div>
            )
        }

        switch (noteType) {
            case "event": {
                return eventInterface();
            }
            case "task": {
                return taskInterface();
            }
            case "memo": {
                return memoInterface();
            }
            default:
                return null;
        }
    }

    function insertNoteHandler() {
        if (note.categoryId.startsWith("google")) {
            axios(insertNote(note, allDay))
                .then(response => {
                    // console.log(response.data);
                    axios.post("/notes/note", {
                        "observe": window.sessionStorage.getItem("observe"),
                        "categoryId": note.categoryId,
                        "note": response.data,
                    })
                        .then(answer => {
                            if (answer.data == true) {
                                alert("노트 등록 완료!");
                                closeModal();
                            } else alert("노트 등록 실패 : DB 문제")
                        })
                        .catch((e) => {
                            alert("노트 등록 실패 : ", e)
                        })
                })
        } else {
            axios.post("/notes/note", {
                "observe": window.sessionStorage.getItem("observe"),
                "note": note,
            })
                .then(answer => {
                    if (answer.data == true) {
                        alert("노트 등록 완료!");
                        closeModal();
                    } else {
                        alert("노트 등록 실패 : DB 문제")
                    }
                })
                .catch((e) => {
                    alert("노트 등록 실패 : ", e)
                })
        }
    }

    function patchNoteHandler() {
        if (note.categoryId.startsWith("google")) {
            axios(patchNote(note, allDay))
                .then(response => {
                    // console.log(response.data);
                    axios.patch("/notes/note", {
                        "observe": window.sessionStorage.getItem("observe"),
                        "categoryId": note.categoryId,
                        "note": response.data,
                    })
                        .then(answer => {
                            if (answer.data == true) {
                                alert("노트 수정 완료!");
                                closeModal();
                            } else alert("노트 수정 실패 : DB 문제")
                        })
                        .catch((e) => {
                            alert("노트 수정 실패 : ", e)
                        })
                })
        } else {
            axios.patch("/notes/note", {
                "observe": window.sessionStorage.getItem("observe"),
                "note": note,
            })
                .then(answer => {
                    if (answer.data == true) {
                        alert("노트 수정 완료!");
                        closeModal();
                    } else {
                        alert("노트 수정 실패 : DB 문제")
                    }
                })
                .catch((e) => {
                    alert("노트 수정 실패 : ", e)
                })
        }
    }

    function deleteNoteHandler() {
        if (window.confirm("정말 삭제하시겠습니까?")) {
            if (note.categoryId.startsWith("google")) {
                axios(deleteNote(note)).then(response => {
                    if (response.status === 204) {
                        axios.delete("notes/note", {
                            data: {
                                "observe": window.sessionStorage.getItem("observe"),
                                "note": note,
                            },
                        }).then(answer => {
                            if (answer.data == true) {
                                alert("노트 삭제 완료!");
                                closeModal();
                            } else {
                                alert("노트 삭제 실패 : DB 문제")
                            }
                        }).catch((e) => {
                            alert("삭제 실패")
                        });
                    } else {
                        console.log("삭제 실패 : 구글");
                    }
                })
            } else {
                axios.delete("notes/note", {
                    data: {
                        "observe": window.sessionStorage.getItem("observe"),
                        "note": note,
                    },
                }).then(answer => {
                    if (answer.data == true) {
                        alert("노트 삭제 완료!");
                        closeModal();
                    } else {
                        alert("노트 삭제 실패 : DB 문제")
                    }
                }).catch((e) => {
                    alert("삭제 실패")
                });
            }
        } else {
            return;
        }
    }

    return (<span className={"noteContainerOuter"}>
            {noteRef ? notePromise() : noteEmpty()}
        </span>)
}