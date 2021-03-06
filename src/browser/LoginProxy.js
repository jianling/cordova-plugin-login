/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/


var Login = {
    getCookie: function(success, fauilure, args) {
        var mockupCookie = 'BIDUPSID=111; BAIDUID=11:FG=1; BDUSS=AAAAAA; bce-auth-type=PASSPORT; bce-user-info="2017-07-06T12:22:12Z|4314321432"; bce-login-type=PASSPORT; bce-verify-status=PASS|fdsafdsafds; bce-login-expire-time="2017-07-06T12:52:17Z|fdsafdsafdsaf"; bce-login-display-name=aaa; bce-service-type="BOS,BCC,BLB,BMR,SCS,CDN,RDS,SES,SMS|fdsafdsa"';

        return success({
            cookie: /localhost/.test(window.location.href) ? mockupCookie : document.cookie
        });
    },
    getCookieValue: function(success, fauilure, args) {
        var mockupCookie = 'BIDUPSID=111; BAIDUID=11:FG=1; BDUSS=AAAAAA; bce-auth-type=PASSPORT; bce-user-info="2017-07-06T12:22:12Z|4314321432"; bce-login-type=PASSPORT; bce-verify-status=PASS|fdsafdsafds; bce-login-expire-time="2017-07-06T12:52:17Z|fdsafdsafdsaf"; bce-login-display-name=aaa; bce-service-type="BOS,BCC,BLB,BMR,SCS,CDN,RDS,SES,SMS|fdsafdsa"';
        var cookie = /localhost/.test(window.location.href) ? mockupCookie : document.cookie;
        var cookieValue = '';
        var cookies = cookie.split(';');
        var key = args[1];
        var reg = new RegExp('(' + key + '=|\")', 'g');

        for(var i = 0; i < cookies.length; i++) {
            if(cookies[i].indexOf(key) >= 0) {
                cookieValue = cookies[i].replace(reg, '').trim();
            }
        }

        return success({
            cookieValue: cookieValue
        });
    },
    showLoginView: function() {
        window.location.href = "https://login.bce.baidu.com?redirect=" + window.location.href;
    },
    logout: function() {
        window.location.href = "https://login.bce.baidu.com?redirect=https://cloud.baidu.com";
    }
};

module.exports = Login;

require("cordova/exec/proxy").add("Login", module.exports);
