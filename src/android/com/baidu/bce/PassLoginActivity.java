package com.baidu.bce;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.baidu.sapi2.PassportSDK;
import com.baidu.sapi2.SapiAccount;
import com.baidu.sapi2.SapiAccountManager;
import com.baidu.sapi2.dto.WebLoginDTO;
import com.baidu.sapi2.shell.listener.WebAuthListener;
import com.baidu.sapi2.shell.result.WebAuthResult;

public class PassLoginActivity extends Activity {


    private WebAuthListener webAuthListener = new WebAuthListener() {

        // 授权(登录或者注册)成功
        @Override
        public void onSuccess(WebAuthResult result) {
            Toast.makeText(PassLoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
            SapiAccount account = SapiAccountManager.getInstance().getSession();
            Intent intent = new Intent();
            intent.putExtra("bduss", account.bduss);
            setResult(RESULT_OK, intent);
            finish();
        }

        // 授权(登录或者注册)失败
        @Override
        public void onFailure(WebAuthResult result) {
            setResult(RESULT_CANCELED);
            String msg = String.format("登录失败(%d:%s)", result.getResultCode(), result.getResultMsg());
            Toast.makeText(PassLoginActivity.this, msg, Toast.LENGTH_SHORT).show();
            finish();
        }

        @Override
        public void beforeSuccess(SapiAccount session) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PassportSDK passportSDK = PassportSDK.getInstance();
        WebLoginDTO webLoginDTO  = new WebLoginDTO();
        webLoginDTO.loginType = WebLoginDTO.EXTRA_LOGIN_WITH_USERNAME;
        passportSDK.startLogin(webAuthListener, webLoginDTO);
    }
}
