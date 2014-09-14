package org.keycloak.keycloakaccountprovider.token;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.util.Base64;
import android.util.Pair;

import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.impl.http.HttpRestProvider;
import org.json.JSONObject;
import org.keycloak.keycloakaccountprovider.KeyCloak;
import org.keycloak.keycloakaccountprovider.KeyCloakAccount;
import org.keycloak.keycloakaccountprovider.util.IOUtils;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Summers on 9/13/2014.
 */
public class AccessTokenExchangeLoader extends AsyncTaskLoader<KeyCloakAccount> {


    private final KeyCloak kc;
    private final String accessToken;
    private KeyCloakAccount account;

    public AccessTokenExchangeLoader(Context context, String accessToken) {
        super(context);
        this.kc = new KeyCloak(context);
        this.accessToken = accessToken;
    }

    @Override
    public KeyCloakAccount loadInBackground() {

        final Map<String, String> data = new HashMap<String, String>();
        data.put("code", accessToken);
        data.put("client_id", kc.getClientId());
        data.put("redirect_uri", kc.getRedirectUri());

        data.put("grant_type", "authorization_code");
        if (kc.getClientSecret() != null) {
            data.put("client_secret", kc.getClientSecret());
        }

        try {
            URL accessTokenEndpoint = new URL(kc.getBaseURL() + "/tokens/access/codes");

            if (kc.getClientSecret() == null) {
                accessTokenEndpoint = new URL(kc.getBaseURL() + "/tokens/access/codes&client_id" + IOUtils.encodeURIComponent(kc.getClientId()));
            }

            final StringBuilder bodyBuilder = new StringBuilder();
            final String formTemplate = "%s=%s";

            String amp = "";
            for (Map.Entry<String, String> entry : data.entrySet()) {
                bodyBuilder.append(amp);
                try {
                    bodyBuilder.append(String.format(formTemplate, entry.getKey(), URLEncoder.encode(entry.getValue(), "UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                amp = "&";
            }

            HttpRestProvider provider = new HttpRestProvider(accessTokenEndpoint);
            provider.setDefaultHeader("Content-Type", "application/x-www-form-urlencoded");

            if (kc.getClientSecret() != null) {
                provider.setDefaultHeader("Authorization", "Basic " + Base64.encodeToString((kc.getClientId() + ":" + kc.getClientSecret()).getBytes("UTF-8"), Base64.DEFAULT | Base64.NO_WRAP));
            }

            HeaderAndBody result = provider.post(bodyBuilder.toString());

            byte[] bodyData = result.getBody();
            String body = new String(bodyData);
            JSONObject accessResponse = new JSONObject(body);
            KeyCloakAccount account = new KeyCloakAccount();
            account.extractTokenProperties(accessResponse);

            return account;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

}
