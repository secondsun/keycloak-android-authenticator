package org.keycloak.keycloakaccountprovider;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.JsonWriter;

import com.google.gson.Gson;

import org.json.JSONObject;

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

        if (options == null || options.getString(KeyCloak.ACCOUNT) == null) {
            toReturn.putParcelable(AccountManager.KEY_INTENT, new Intent(context, KeycloakAuthenticationActivity.class).putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response));
            toReturn.putParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        } else {
            KeyCloakAccount account = new Gson().fromJson(options.getString(KeyCloak.ACCOUNT), KeyCloakAccount.class);
            writeAccount(account);
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
        String username = account.name;
        KeyCloakAccount kcAccount = readAccount(username);
        Bundle toReturn = new Bundle();
        toReturn.putString(AccountManager.KEY_AUTHTOKEN, kcAccount.getAccessToken());
        toReturn.putString(AccountManager.KEY_ACCOUNT_NAME, kcAccount.getPreferredUsername());
        toReturn.putString(AccountManager.KEY_ACCOUNT_TYPE, KeyCloak.ACCOUNT_TYPE);
        return toReturn;
    }

    private KeyCloakAccount readAccount(String username) {
        SharedPreferences prefs = context.getSharedPreferences("KeyCloak", Context.MODE_PRIVATE);
        String jsonAccount = prefs.getString(username, null);

        if (jsonAccount == null) {
            return null;
        }

        return new Gson().fromJson(jsonAccount, KeyCloakAccount.class);

    }

    @Override
    public String getAuthTokenLabel(String s) {
        return "KeyCloak Token";
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
        return null;
    }

    private void writeAccount(KeyCloakAccount account) {
        SharedPreferences prefs = context.getSharedPreferences("KeyCloak", Context.MODE_PRIVATE);
        prefs.edit().putString(account.getPreferredUsername(), new Gson().toJson(account)).commit();
    }

}
