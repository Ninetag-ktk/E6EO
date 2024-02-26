import React, {useState, useEffect} from 'react';
import axios from 'axios'
import './App.css';
import logo from './temp_logo.png'
import {
    createBrowserRouter,
    createRoutesFromElements,
    Route,
    RouterProvider,
    Routes,
    Link
} from "react-router-dom";
import Create from "./Create";
import {Axios} from "axios";


const Login =() => {

    return (
        <div className={"login-main"}>
            <div className={"Logoclass"}>
                <img src={logo}/>
            </div>

            <div className={"logininput"}>
                <form className={"loginForm1"} action={"/user/test"} method={"post"}>
                    <input type={"text"} className={"inputtext"} placeholder={"ex)XXXXXX@xxxxxx.com"}/>
                    <input type={"text"} className={"inputtext"} placeholder={"password"}/>
                    <button type={"button"}>로그인</button>

                </form>
                <Link to="/create">회원가입</Link>
                <hr/>
                <button>google계정으로 로그인</button>
            </div>
            <div>
                <Routes>
                    <Route path="/create" element={<Create/>}/>
                </Routes>
            </div>

        </div>
    );
};

export default Login;