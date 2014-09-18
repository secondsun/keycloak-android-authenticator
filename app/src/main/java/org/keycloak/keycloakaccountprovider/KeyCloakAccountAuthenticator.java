package org.keycloak.keycloakaccountprovider;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;

import com.google.gson.Gson;

import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.http.HttpException;
import org.jboss.aerogear.android.impl.http.HttpRestProvider;
import org.json.JSONObject;
import org.keycloak.keycloakaccountprovider.util.IOUtils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Summers on 9/12/2014.
 */
public class KeyCloakAccountAuthenticator  extends AbstractAccountAuthenticator {

    private final Context context;

    public KeyCloakAccountAuthenticator(Context context) {
        super(context);
        this.context = context.getApplicationContext();
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Bundle toReturn = new Bundle();

        if (options == null || options.getString(KeyCloak.ACCOUNT_KEY) == null) {
            toReturn.putParcelable(AccountManager.KEY_INTENT, new Intent(context, KeycloakAuthenticationActivity.class).putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response));
            toReturn.putParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        } else {
            KeyCloakAccount account = new Gson().fromJson(options.getString(KeyCloak.ACCOUNT_KEY), KeyCloakAccount.class);
            AccountManager.get(context).addAccountExplicitly(new Account(account.getPreferredUsername(), KeyCloak.ACCOUNT_TYPE), null, options);
            toReturn.putString(AccountManager.KEY_ACCOUNT_NAME, account.getPreferredUsername());
            toReturn.putString(AccountManager.KEY_ACCOUNT_TYPE, KeyCloak.ACCOUNT_TYPE);

        }

        return toReturn;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {

        AccountManager am = AccountManager.get(context);
        String keyCloackAccount = am.getUserData(account, KeyCloak.ACCOUNT_KEY);
        KeyCloakAccount kcAccount = new Gson().fromJson(keyCloackAccount, KeyCloakAccount.class);

        if (new Date(kcAccount.getExpiresOn()).before(new Date())) {
            try {
                refreshToken(kcAccount);
                String accountJson = new Gson().toJson(kcAccount);
                am.setUserData(new Account(kcAccount.getPreferredUsername(), KeyCloak.ACCOUNT_TYPE), KeyCloak.ACCOUNT_KEY, accountJson);
            } catch (HttpException e) {
                if (e.getStatusCode() / 100 == 4) {
                    Bundle toReturn = new Bundle();
                    toReturn.putParcelable(AccountManager.KEY_INTENT, new Intent(context, KeycloakAuthenticationActivity.class).putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse));
                    toReturn.putParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);
                    return toReturn;
                }
            }
        }

        Bundle toReturn = new Bundle();
        toReturn.putString(AccountManager.KEY_AUTHTOKEN, kcAccount.getAccessToken());
        toReturn.putString(AccountManager.KEY_ACCOUNT_NAME, kcAccount.getPreferredUsername());
        toReturn.putString(AccountManager.KEY_ACCOUNT_TYPE, KeyCloak.ACCOUNT_TYPE);
        return toReturn;
    }


    @Override
    public String getAuthTokenLabel(String s) {
        return "KeyCloak Token";
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {

        String keyCloackAccount = AccountManager.get(context).getUserData(account, KeyCloak.ACCOUNT_KEY);
        KeyCloakAccount kca = new Gson().fromJson(keyCloackAccount, KeyCloakAccount.class);

        if (kca.getExpiresOn() < new Date().getTime()) {
            throw new RuntimeException("token expired");
        }

        Bundle toReturn = new Bundle();
        toReturn.putString(AccountManager.KEY_ACCOUNT_NAME, kca.getPreferredUsername());
        toReturn.putString(AccountManager.KEY_ACCOUNT_TYPE, KeyCloak.ACCOUNT_TYPE);

        return toReturn;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
        return null;
    }

    private KeyCloakAccount refreshToken(KeyCloakAccount account) throws NetworkErrorException {

        KeyCloak kc = new KeyCloak(context);

        final Map<String, String> data = new HashMap<String, String>();
        data.put("refresh_token", account.getRefreshToken());
        data.put("grant_type", "refresh_token");


        try {
            URL accessTokenEndpoint = new URL(kc.getBaseURL() + "/tokens/refresh");

            if (kc.getClientSecret() == null) {
                accessTokenEndpoint = new URL(kc.getBaseURL() + "/tokens/refresh&client_id=" + IOUtils.encodeURIComponent(kc.getClientId()));
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
            account.extractTokenProperties(accessResponse);

            return account;
        } catch (HttpException e) {
            throw e;
        } catch (Exception e) {
            throw new NetworkErrorException(e);
        }


    }

}
