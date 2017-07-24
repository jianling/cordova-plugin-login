package com.baidu.bce;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.baidu.sapi2.SapiAccount;
import com.baidu.sapi2.SapiAccountManager;
import com.baidu.sapi2.SapiWebView;
import com.baidu.sapi2.shell.listener.AuthorizationListener;

public class LoginActivity extends Activity {

    private SapiWebView sapiWebView;

    private AuthorizationListener authorizationListener = new AuthorizationListener() {

        // 授权(登录或者注册)成功
        @Override
        public void onSuccess() {
            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
            SapiAccount account = SapiAccountManager.getInstance().getSession();
            Intent intent = new Intent();
            intent.putExtra("bduss", account.bduss);
            setResult(RESULT_OK, intent);
            finish();
        }

        // 授权(登录或者注册)失败
        @Override
        public void onFailed(int errorNo, String errorMsg) {
            setResult(RESULT_CANCELED);
            String msg = String.format("登录失败(%d:%s)", errorNo, errorMsg);
            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sapiWebView = (SapiWebView) findViewById(R.id.sapi_webview);

        // 设置登录/注册/第三方帐号登录等帐号操作成功或者失败之后的回调；
        sapiWebView.setAuthorizationListener(authorizationListener);

        sapiWebView.setOnFinishCallback(new SapiWebView.OnFinishCallback() {
            @Override
            public void onFinish() {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        // 设置系统back回调
        sapiWebView.setOnNewBackCallback(new SapiWebView.OnNewBackCallback() {
            @Override
            public boolean onBack() {
                if (sapiWebView.canGoBack()) {
                    sapiWebView.goBack();
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
                return false;
            }
        });

        sapiWebView.loadLogin();
    }
}
