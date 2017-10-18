/**
 * Baidu passport Login plugin for Cordova / Phonegap
 */
package com.baidu;


import android.content.Intent;
import android.util.Log;
import android.webkit.CookieManager;

import com.baidu.bce.LoginActivity;
import com.baidu.sapi2.SapiAccountManager;
import com.github.kevinsawicki.http.HttpRequest;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;

public class Login extends CordovaPlugin {

    final String postLoginUrl =
            "https://login.bce.baidu.com/postlogin?_1495851167415&redirect=http%3A%2F%2Fconsole.bce.baidu.com";

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        if (action.equals("showLoginView")) {
            final CordovaInterface cordovaInt = cordova;

            Intent intent = new Intent(cordova.getActivity(), LoginActivity.class);
            cordova.startActivityForResult(new CordovaPlugin() {
                @Override
                public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                    if (intent != null) {
                        String bduss = intent.getExtras().getString("bduss");

                        Map paramsMap = new HashMap<String, String>();
                        Map headersMap = new HashMap<String, String>();

                        headersMap.put("Cookie", "BDUSS=" + bduss + "; bce-login-type=PASSPORT;");

                        PostLogin get = new PostLogin(postLoginUrl, paramsMap, headersMap, callbackContext);
                        cordovaInt.getThreadPool().execute(get);
                    }
                }
            }, intent, 1);
        }
        else if (action.equals("getCookie")) {
            final String url = args.getString(0);

            Runnable getCookieRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        CookieManager cookieManager = CookieManager.getInstance();

                        String cookies = cookieManager.getCookie(url);

                        JSONObject cookiesObject = new JSONObject();
                        cookiesObject.put("cookie", cookies);

                        callbackContext.success(cookiesObject);
                    } catch (Exception e) {
                        callbackContext.error("");
                    }
                }
            };

            cordova.getThreadPool().execute(getCookieRunnable);
        }
        else if (action.equals("getCookieValue")) {
            final String url = args.getString(0);
            final String key = args.getString(1);

            Runnable getCookieRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        CookieManager cookieManager = CookieManager.getInstance();
                        String[] cookies = cookieManager.getCookie(url).split("; ");
                        String cookieValue = "";

                        for (String cookie : cookies) {
                            if (cookie.contains(key + "=")) {
                                cookieValue = cookie.split("=")[1].trim();
                                break;
                            }
                        }

                        JSONObject cookieValueObject = new JSONObject();
                        cookieValueObject.put("cookieValue", cookieValue);

                        callbackContext.success(cookieValueObject);
                    } catch (Exception e) {
                        callbackContext.error("");
                    }
                }
            };

            cordova.getThreadPool().execute(getCookieRunnable);
        }
        else if (action.equals("logout")) {
            String logoutUrl = "https://login.bce.baidu.com/logout";
            Map paramsMap = new HashMap<String, String>();
            Map headersMap = new HashMap<String, String>();

            Logout get = new Logout(logoutUrl, paramsMap, headersMap, callbackContext);
            cordova.getThreadPool().execute(get);
        }

        return true;
    }

    @Override
    public void onResume(boolean multitasking) {
        Runnable postLoginRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String bduss = SapiAccountManager.getInstance().getSession(SapiAccountManager.SESSION_BDUSS);

                    Map paramsMap = new HashMap<String, String>();
                    Map headersMap = new HashMap<String, String>();

                    headersMap.put("Cookie", "BDUSS=" + bduss + "; bce-login-type=PASSPORT;");

                    HttpRequest request = HttpRequest.get(postLoginUrl, paramsMap, false);

                    request.followRedirects(false);
                    request.acceptCharset("UTF-8");
                    request.headers(headersMap);

                    request.code();

                    // set cookies
                    List<String> cookies = request.getConnection().getHeaderFields().get("Set-Cookie");
                    if (cookies != null && !cookies.isEmpty()) {
                        CookieManager cookieManager = CookieManager.getInstance();
                        for (String cookie : cookies) {
                            cookieManager.setCookie("https://console.bce.baidu.com", cookie);
                        }
                    }
                } catch (HttpRequest.HttpRequestException e) {
                    if (e.getCause() instanceof UnknownHostException) {
                        Log.i("UnknownHostException", "The host could not be resolved");
                    } else if (e.getCause() instanceof SSLHandshakeException) {
                        Log.i("SSLHandshakeException", "SSL handshake failed");
                    } else {
                        Log.i("other Exception", "There was an error with the request");
                    }
                }
            }
        };

        cordova.getThreadPool().execute(postLoginRunnable);
    }
}
