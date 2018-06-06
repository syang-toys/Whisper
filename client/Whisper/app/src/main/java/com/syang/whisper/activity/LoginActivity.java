package com.syang.whisper.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.syang.whisper.R;
import com.syang.whisper.WhisperApplication;
import com.syang.whisper.fragment.LoginFragment;
import com.syang.whisper.fragment.RegisterFragment;
import com.syang.whisper.model.Self;
import com.syang.whisper.request.Restful;
import com.syang.whisper.request.SecureCallback;
import com.syang.whisper.request.SecureSocket;
import com.syang.whisper.utils.RSAUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import okhttp3.Call;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private LoginFragment mLoginFragment;
    private RegisterFragment mRegisterFragment;
    private WhisperApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginFragment = new LoginFragment();
        mRegisterFragment = new RegisterFragment();

        app = (WhisperApplication) getApplication();

        startLoginFragment();
    }

    public void startLoginFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mLoginFragment)
                .commit();
    }

    public void startRegisterFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mRegisterFragment)
                .commit();
    }

    public void onLogin(final String email, String password) {
        String body = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

        Restful.post("login", body, new SecureCallback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject body = new JSONObject(Restful.getDecryptBody(response.body().string(), this.getSecretKey()));
                    if (response.code() == 200) {
                        RSAPublicKey publicKey = (RSAPublicKey) RSAUtil.getPublicKey(body.getString("publicKey"));
                        RSAPrivateKey privateKey = (RSAPrivateKey) RSAUtil.getPrivateKey(body.getString("privateKey"));
                        SecureSocket.mDefaultPrivateKey = privateKey;
                        ((Self)app.getSelf()).initial(body.getInt("id"), email, publicKey, privateKey);
                        finish();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setTitle(R.string.login_failed);
                                builder.setMessage(body.optString("code", "unknown error"));
                                builder.setNeutralButton(R.string.oc, null);
                                builder.show();
                            }
                        });
                    }
                } catch (JSONException ex) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, R.string.parse_error, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    public void onRegister(String email, String password, String repeat) {
        if (!password.equals(repeat)) {
            Toast.makeText(getApplicationContext(), "Password doesn't match!", Toast.LENGTH_SHORT).show();
        } else {
            String body = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

            Restful.post("register", body, new SecureCallback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 200) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setTitle(R.string.success);
                                builder.setMessage(R.string.register_success);
                                builder.setNeutralButton(R.string.oc, null);
                                builder.show();
                            }
                        });
                        startLoginFragment();
                    } else {
                        try {
                            final JSONObject body = new JSONObject(response.body().string());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                    builder.setTitle(R.string.register_failed);
                                    builder.setMessage(body.optString("code", "unknown error"));
                                    builder.setNeutralButton(R.string.oc, null);
                                    builder.show();
                                }
                            });
                        } catch (JSONException ex) {
                            Toast.makeText(LoginActivity.this, R.string.parse_error, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }
    }
}