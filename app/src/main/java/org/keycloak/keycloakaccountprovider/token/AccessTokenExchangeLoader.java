package org.keycloak.keycloakaccountprovider.token;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.util.Base64;
import android.util.Pair;


import org.json.JSONObject;
import org.keycloak.keycloakaccountprovider.KeyCloak;
import org.keycloak.keycloakaccountprovider.KeyCloakAccount;
import org.keycloak.keycloakaccountprovider.util.IOUtils;
import org.keycloak.keycloakaccountprovider.util.TokenExchangeUtils;

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
        return TokenExchangeUtils.exchangeForAccessCode(accessToken, kc);
    }

}
