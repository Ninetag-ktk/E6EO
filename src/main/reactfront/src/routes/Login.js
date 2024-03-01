import React, {useState} from 'react';
import logo from './temp_logo.png'
import {useNavigate} from "react-router-dom";


export default function Login() {
    const redirect = useNavigate();
    const [loginInfo, setLoginInfo] = useState({
        id: "",
        pw: "",
    });
    const [autoLogin, setAutoLogin] = useState(false);

    const handleLogin = async () => {
        const response = await fetch("/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json; charset=utf-8",
                "Accept": "application/json; charset=utf-8",
            },
            body: JSON.stringify(loginInfo),
        });
        const result = await response.json();
        if (result.code === "200") {
            // 로그인 성공 처리
            redirect(`/check?autologin=${autoLogin}&observe=${result.body}`,);
        } else {
            // 로그인 실패 처리
            alert(result.body);
            // window.location.href = "/";
        }
    };

    const handleGoogleLogin = async () => {
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

    return (
        <div className={"loginall"}>
            <div className={"login-main"}>
                <div className={"Logoclass"}>
                    <img src={logo}/>
                </div>
                <div className={"logininput"}>
                    <div className={"loginForm"}>
                        <input
                            type="text"
                            className="inputtext"
                            name="id"
                            placeholder="email"
                            value={loginInfo.id}
                            onChange={(e) => setLoginInfo({...loginInfo, id: e.target.value})}
                        />
                        <input
                            type="password"
                            className="inputtext"
                            name="pw"
                            placeholder="password"
                            value={loginInfo.pw}
                            onChange={(e) => setLoginInfo({...loginInfo, pw: e.target.value})}
                        />
                        <input
                            type={"checkbox"} checked={autoLogin}
                            onChange={(e) => setAutoLogin(e.target.checked)}
                        />로그인유지
                        <button className={"btn"} type={"submit"} onClick={handleLogin}>로그인</button>
                    </div>
                    <button className={"btn"}>회원가입</button>
                    <hr/>
                    <button className={"btn"} onClick={handleGoogleLogin}>google계정으로 로그인</button>
                </div>
            </div>
        </div>
    )
}

