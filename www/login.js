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

/* global cordova */

var exec = require('cordova/exec');


var Login = {
    getCookie: function(url, success, failure) {
        return exec(success, failure, "Login", "getCookie", [url]);
    },
    getCookieValue: function(url, cookieKey, success, failure) {
        return exec(success, failure, "Login", "getCookieValue", [url, cookieKey]);
    },
    showLoginView: function(success, failure) {
        return exec(success, failure, "Login", "showLoginView");
    },
    showPassLoginView: function(success, failure) {
        return exec(success, failure, "Login", "showPassLoginView");
    },
    showUCLoginView: function(success, failure) {
        return exec(success, failure, "Login", "showUCLoginView");
    },
    ucLoginSuccess: function(opts, success, failure) {
        return exec(success, failure, "Login", "ucLoginSuccess", [opts]);
    },
    popView: function(success, failure) {
        return exec(success, failure, "Login", "popView");
    },
    logout: function(success, failure) {
        return exec(success, failure, "Login", "logout");
    }
};

module.exports = Login;
