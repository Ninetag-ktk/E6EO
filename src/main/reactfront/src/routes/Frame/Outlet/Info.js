import React, {useEffect, useState} from 'react';
import $ from 'jquery';
import logo from '../../temp_logo.png';
import axios from "axios";
import {useNavigate} from "react-router-dom";

export default function SignupForm() {
    const redirect = useNavigate();
    const [userid, setUserid] = useState('');
    const [innerid, setInnerid] = useState('');
    const [nickname, setNickname] = useState('');
    const [password, setPassword] = useState('');
    const [passwordTwin, setPasswordTwin] = useState('');
    const observe = window.sessionStorage.getItem("observe");
    

    useEffect(() => {
        axios("/user/update", {
            method: "POST",
            headers: {
                "Content-Type": "application/json; charset=utf-8",
                "Accept": "application/json; charset=utf-8",
            },
            data: observe,
        }).then(response => {
            console.log(response);
            if (response.data != null) {
                const user = response.data;
                setUserid(user.userId);
                setInnerid(user.innerId);
                setNickname(user.nickName);
                setPassword(user.pw);
                setPasswordTwin(user.pw);
            }
        });
    }, [])

    const handleDisconnectGoogle = () => {
        var answer = prompt('정말 연동 해제하시겠습니까?\n연동 해제하시려면 \<연동 해제\> 라고 입력해주세요', '');
        if (answer === "연동 해제") {
            axios("/user/google", {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json; charset=utf-8",
                },
                data: window.sessionStorage.getItem("observe")
            });
            window.sessionStorage.removeItem("token");
            window.location.reload();
        }
    }

    /*사후 연동*/
    const handleConnectGoogle = async () => {
        const response = await fetch("/google/login", {
            mode: "no-cors",
            headers: {
                "Content-Type": "application/json; charset=utf-8",
                "Accept": "application/json; charset=utf-8",
            },
        });
        const result = await response.json();
        window.location.href = result.redirect;
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        // Parsley.js 유효성 검사 (초기화가 완료된 가정)
        if (!$('#signup-form').parsley().validate()) {
            return;
        }

        // 회원가입 API 호출
        axios("/user/info", {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json; charset=utf-8",
                "Accept": "application/json; charset=utf-8",
            },
            data: {
                userId: userid,
                pw: password,
                nickName: nickname,
                observeToken: window.sessionStorage.getItem("observe"),
            }
        }).then(response => {
            if (response.data === true) {
                if (password !== passwordTwin) {
                    alert("수정된 비밀번호로 다시 로그인 해주세요");
                    window.localStorage.removeItem("observe");
                    window.sessionStorage.removeItem("observe");
                    redirect("/");
                } else {
                    window.location.reload();
                }
            } else {
                alert("회원정보 수정에 실패했습니다")
            }
        })
    };

    const handleExpire = () => {
        var answer = prompt('정말 탈퇴하시겠습니까?\n탈퇴 하시려면 \<회원 탈퇴\> 라고 입력해주세요', '');
        if (answer === "회원 탈퇴") {
            axios("/user/info", {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json; charset=utf-8",
                    "Accept": "application/json; charset=utf-8",
                },
                data: window.sessionStorage.getItem("observe"),
            }).then(response => {
                if (response.data == true) {
                    window.localStorage.removeItem("observe");
                    window.sessionStorage.removeItem("observe");
                    alert("정상적으로 탈퇴되었습니다.\n이용해 주셔서 감사합니다");
                    redirect("/");
                } else {
                    alert("탈퇴에 실패했습니다.\n관리자에게 문의해주세요");
                }
            })
        }
    }


    useEffect(() => {
        $('#signup-form').parsley();
    }, []);

    return (
        <div id={"userInfo"} className={"info"}>
            <a href={"/main"}> <img className={"createlogo"} src={logo}/> </a>
            <form id="signup-form" onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="userid">계정</label>
                    <input
                        type="text"
                        name="userid"
                        id="userid"
                        readOnly={true}
                        value={userid}
                        data-parsley-required
                        data-parsley-minlength="1"
                        data-parsley-maxlength="20"
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="innerid">Google</label>
                    <input
                        type="text"
                        name="innerid"
                        id="innerid"
                        readOnly={true}
                        value={innerid}
                        data-parsley-minlength="1"
                        data-parsley-maxlength="20"
                    />
                    {innerid == null ?
                        <button type="button" onClick={handleConnectGoogle}>구글 연동</button> :
                        <button type="button" onClick={handleDisconnectGoogle}>연동 해제</button>}

                </div>
                <div className="form-group">
                    <label htmlFor="nickname">닉네임</label>
                    <input
                        type="text"
                        name="nickname"
                        id="nickname"
                        value={nickname}
                        onChange={(e) => setNickname(e.target.value)}
                        data-parsley-required
                        data-parsley-minlength="1"
                        data-parsley-maxlength="20"
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="password">비밀번호(대문자,소문자,숫자,특수문자를 포함해야합니다.)</label>
                    <input
                        type="password"
                        name="password"
                        id="password"
                        placeholder="비밀번호를 입력하세요"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        data-parsley-minlength="8"
                        data-parsley-uppercase="1"
                        data-parsley-lowercase="1"
                        data-parsley-number="1"
                        data-parsley-special="1"
                        data-parsley-required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="password-confirm">비밀번호 확인</label>
                    <input
                        type="password"
                        name="password-confirm"
                        id="password-confirm"
                        placeholder="비밀번호를 입력하세요"
                        data-parsley-required
                        data-parsley-equalto="#password"
                        data-parsley-error-message="비밀번호가 일치하지 않습니다."
                    />
                </div>
                <button type="submit">회원정보저장</button>
                <button type="button" onClick={handleExpire}>회원 탈퇴</button>
            </form>
        </div>
    );
}