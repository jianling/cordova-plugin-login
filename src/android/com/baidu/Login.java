/**
 * Baidu passport Login plugin for Cordova / Phonegap
 */
package com.baidu;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.text.TextUtils;
import android.widget.Toast;
import android.app.Activity;

import com.baidu.bce.LoginActivity;
import com.baidu.bce.PassLoginActivity;
import com.baidu.bce.UcLoginActivity;
import com.baidu.sapi2.PassportSDK;
import com.baidu.sapi2.SapiAccountManager;
import com.baidu.sapi2.activity.LoadExternalWebViewActivity;
import com.baidu.sapi2.callback.AccountRealNameCallback;
import com.baidu.sapi2.utils.SapiUtils;
import com.github.kevinsawicki.http.HttpRequest;
import com.zxing.activity.CaptureActivity;

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

    CallbackContext loginCallbackContext;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        if (action.equals("showLoginView")) {
            this.showLoginView(action, args, callbackContext);
        }
        else if (action.equals("showPASSQRCodeScanViewController")) {
            this.showPASSQRCodeScanViewController(action, args, callbackContext);
        }
        else if (action.equals("showPASSAccountRealNameViewController")) {
            this.showPASSAccountRealNameViewController(action, args, callbackContext);
        }
        else if (action.equals("showPassLoginView")) {
            this.showPassLoginView(action, args, callbackContext);
        }
        else if (action.equals("showUCLoginView")) {
            this.showUCLoginView(action, args, callbackContext);
        }
        else if (action.equals("showUCRegisterView")) {
            this.showUCRegisterView();
        }
        else if (action.equals("ucLoginSuccess")) {
            this.ucLoginSuccess(action, args, callbackContext);
        }
        else if (action.equals("getCookie")) {
            this.getCookie(action, args, callbackContext);
        }
        else if (action.equals("getCookieValue")) {
            this.getCookieValue(action, args, callbackContext);
        }
        else if (action.equals("popView")) {
            this.popView(action, args, callbackContext);
        }
        else if (action.equals("logout")) {
            this.logout(action, args, callbackContext);
        }

        return true;
    }

    private void showLoginView(final String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        loginCallbackContext = callbackContext;

        Intent intent = new Intent(this.cordova.getActivity(), LoginActivity.class);
        intent.putExtra("callbackId", callbackContext.getCallbackId());

        cordova.startActivityForResult(new CordovaPlugin() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                if (intent != null && resultCode == android.app.Activity.RESULT_OK) {
                    String loginType = intent.getExtras().getString("loginType");
                    try {
                        if (loginType.equals("uc")) {
                            showUCLoginView(action, args, callbackContext);
                        }
                        else {
                            showPassLoginView(action, args, callbackContext);
                        }
                    } catch (JSONException e) {
                        // catch JSONException
                    }
                }
            }
        }, intent, 1);
    }

    private void showPASSQRCodeScanViewController(
            final String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        final CordovaInterface cordovaInt = cordova;
        final CordovaPlugin self = this;
        Intent intent = new Intent(this.cordova.getActivity(), CaptureActivity.class);

        cordova.startActivityForResult(new CordovaPlugin() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                if (resultCode == Activity.RESULT_OK) {
                    final String result = intent.getStringExtra(CaptureActivity.BUNDLE_SCAN_RESULT_TEXT);
                    String url = SapiUtils.parseQrFaceAuthSchema(result);
                    if (TextUtils.isEmpty(url)) {
                        Toast.makeText(cordovaInt.getActivity(), "抱歉，您扫描的二维码有误，请重新扫描", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent newIntent = new Intent(cordovaInt.getActivity(), LoadExternalWebViewActivity.class);
                    newIntent.putExtra(LoadExternalWebViewActivity.EXTRA_EXTERNAL_TITLE, "帐号实名");
                    newIntent.putExtra(LoadExternalWebViewActivity.EXTRA_EXTERNAL_URL, url);
                    cordovaInt.startActivityForResult(self, newIntent, 1);
                }
            }
        }, intent, 1);
    }


    private void showPASSAccountRealNameViewController(
            final String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {

        PassportSDK passportSDK = PassportSDK.getInstance();
        passportSDK.loadAccountRealName(new AccountRealNameCallback() {
            @Override
            public void onFinish() {
                callbackContext.success();
            }
        }, SapiAccountManager.getInstance().getSession(SapiAccountManager.SESSION_BDUSS));
    }

    private void showPassLoginView(String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        final CordovaInterface cordovaInt = cordova;

        Intent intent = new Intent(cordova.getActivity(), PassLoginActivity.class);
        cordova.startActivityForResult(new CordovaPlugin() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                if (intent != null && resultCode == Activity.RESULT_OK) {
                    String bduss = intent.getExtras().getString("bduss");

                    Map paramsMap = new HashMap<String, String>();
                    Map headersMap = new HashMap<String, String>();

                    headersMap.put("Cookie", "BDUSS=" + bduss + "; bce-login-type=PASSPORT;");

                    PostLogin get = new PostLogin(postLoginUrl,
                            paramsMap, headersMap, callbackContext, cordovaInt.getActivity());
                    cordovaInt.getThreadPool().execute(get);
                }
            }
        }, intent, 1);
    }

    private void showUCLoginView(String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        Intent intent = new Intent(this.cordova.getActivity(), UcLoginActivity.class);
        intent.putExtra("pageUrl", "https://login.bce.baidu.com");
        intent.putExtra("title", "云账号登录");
        intent.putExtra("configFile", "uc_login_config.xml");
        cordova.startActivityForResult(new CordovaPlugin() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                if (intent != null && resultCode == Activity.RESULT_OK) {
                    loginCallbackContext.success();
                }
            }
        }, intent, 1);
    }

    public void showUCRegisterView() {
        Intent intent = new Intent(this.cordova.getActivity(), MultiviewActivity.class);
        intent.putExtra("pageUrl", "uc-reg/index.html");
        intent.putExtra("title", "注册");
        intent.putExtra("configFile", "uc_reg_config.xml");
        cordova.startActivityForResult(this, intent, 1);
    }

    private void ucLoginSuccess(String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        final CordovaInterface cordovaInt = cordova;

        Map paramsMap = new HashMap<String, String>();
        Map headersMap = new HashMap<String, String>();

        CookieManager cookieManager = CookieManager.getInstance();

        String cookies = cookieManager.getCookie(postLoginUrl);

        headersMap.put("Cookie", cookies);

        PostUcLogin postUcLoginRequest = new PostUcLogin(postLoginUrl,
                paramsMap, headersMap, callbackContext, cordova.getActivity());
        cordova.getThreadPool().execute(postUcLoginRequest);

        postUcLoginRequest.setLoginSuccessListener(new OnLoginSuccessListener() {
            public void onLoginSuccess() {
                cordovaInt.getActivity().setResult(cordovaInt.getActivity().RESULT_OK, new Intent());
                cordovaInt.getActivity().finish();
            }
        });
    }

    private void getCookie(String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        final String url = args.getString(0);

        Runnable getCookieRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String cookies = Util.getCookie(url);

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

    private void getCookieValue(String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        final String url = args.getString(0);
        final String key = args.getString(1);

        Runnable getCookieRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String cookieValue = Util.getCookieValue(url, key);

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

    private void popView(String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        cordova.getActivity().finish();
    }

    private void logout(String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        String logoutUrl = "https://login.bce.baidu.com/logout";
        Map paramsMap = new HashMap<String, String>();
        Map headersMap = new HashMap<String, String>();

        Logout get = new Logout(logoutUrl, paramsMap, headersMap, callbackContext);
        cordova.getThreadPool().execute(get);
    }

    @Override
    public void onResume(boolean multitasking) {
        Runnable postLoginRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String bduss = SapiAccountManager.getInstance().getSession(SapiAccountManager.SESSION_BDUSS);

                    if (bduss == null || Util.getCookieValue("https://console.bce.baidu.com", "bce-auth-type").equals("UC")) {
                        return;
                    }

                    Map paramsMap = new HashMap<String, String>();
                    Map headersMap = new HashMap<String, String>();

                    headersMap.put("Cookie", "BDUSS=" + bduss + "; bce-login-type=PASSPORT;");

                    HttpRequest request = HttpRequest.get(postLoginUrl, paramsMap, false);

                    request.followRedirects(false);
                    request.acceptCharset("UTF-8");
                    request.headers(headersMap);

                    // reset User_agent: bai du yun/1.1.0 (Google; Android 8.0.0)
                    String versionCode = "";
                    Activity cordovaActivity = cordova.getActivity();
                    String packageName = cordovaActivity.getPackageName();
                    try {
                        versionCode = cordovaActivity.getPackageManager().getPackageInfo(packageName, 0).versionName;
                    } catch (PackageManager.NameNotFoundException e) {
                        // e
                    }

                    String agent = "bai du yun/" + versionCode
                            + " (" + Build.MANUFACTURER + "; Android " + Build.VERSION.RELEASE + ")";

                    request.userAgent(agent);

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
