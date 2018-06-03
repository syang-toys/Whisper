package com.syang.whisper.request;

import com.syang.whisper.crypto.AES;
import com.syang.whisper.crypto.RSA;
import com.syang.whisper.crypto.XXTea;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.PublicKey;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class Restful {
    private static final String BASE_URL = "http://10.131.229.156:3000/api/";
    private static final OkHttpClient client = new OkHttpClient();
    private static PublicKey mServerPublicKey;

    public static void setServerPublicKey(PublicKey publicKey) {
        mServerPublicKey = publicKey;
    }

    public static void post(String api, String data, SecureCallback callback) {
        String key = RandomStringUtils.random(32);
        callback.setSecretKey(key);
        RequestBody body = new FormBody.Builder().add("key", RSA.Base64Encrypt(key, mServerPublicKey)).add("data", XXTea.Base64Encrypt(data, key)).build();
        Request request = new Request.Builder().url(BASE_URL+api).post(body).build();
        client.newCall(request).enqueue(callback);
    }

    public static void get(String api, String data, Callback callback) { }

    public static String getDecryptBody(String data, String key) {
        try {
            final JSONObject response = new JSONObject(data);
            final String AESKey = XXTea.Base64Decrypt(response.getString("key"), key);
            final String AESIv = XXTea.Base64Decrypt(response.getString("iv"), key);
            return AES.Base64Decrypt(response.getString("data"), AESKey, AESIv);
        } catch (JSONException ex) {
            return data;
        }
    }
}
