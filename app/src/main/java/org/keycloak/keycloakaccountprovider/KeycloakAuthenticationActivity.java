package org.keycloak.keycloakaccountprovider;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.gson.Gson;

import org.keycloak.keycloakaccountprovider.token.AccessTokenExchangeLoader;
import org.keycloak.keycloakaccountprovider.util.IOUtils;


public class KeycloakAuthenticationActivity extends AccountAuthenticatorActivity implements LoaderManager.LoaderCallbacks<KeyCloakAccount> {

    private KeyCloak kc;
    private static final String ACCESS_TOKEN_KEY = "accessToken";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        kc = new KeyCloak(this);
        Account[] accounts = AccountManager.get(this).getAccountsByType(KeyCloak.ACCOUNT_TYPE);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public Loader<KeyCloakAccount> onCreateLoader(int i, Bundle bundle) {
        return new AccessTokenExchangeLoader(this, bundle.getString(ACCESS_TOKEN_KEY));
    }

    @Override
    public void onLoadFinished(Loader<KeyCloakAccount> keyCloakAccountLoader, KeyCloakAccount keyCloakAccount) {
        AccountAuthenticatorResponse response = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        Bundle accountBundle = new Bundle();
        accountBundle.putString(KeyCloak.ACCOUNT, new Gson().toJson(keyCloakAccount));

        if (response != null) {
            AccountManager.get(this).addAccountExplicitly(new Account(keyCloakAccount.getPreferredUsername(), KeyCloak.ACCOUNT_TYPE), null, accountBundle);
            response.onResult(accountBundle);
            finish();
        } else {
            AccountManager am = AccountManager.get(this);
            am.addAccount(KeyCloak.ACCOUNT_TYPE, null, null, accountBundle, null, null, null);
            finish();
        }


    }

    @Override
    public void onLoaderReset(Loader<KeyCloakAccount> keyCloakAccountLoader) {

    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private KeyCloak kc;
        private WebView webView;

        public PlaceholderFragment() {

        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            if (kc == null) {
                kc = new KeyCloak(activity);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.keycloak_login_fragment, container, false);
            webView = (WebView) rootView.findViewById(R.id.webview);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {

                    if (url.contains("code=")) {
                        final String token = IOUtils.fetchToken(url);
                        Log.d("TOKEN", token);

                        Bundle data = new Bundle();
                        data.putString(ACCESS_TOKEN_KEY, token);
                        getLoaderManager().initLoader(1,data, (LoaderManager.LoaderCallbacks)(getActivity())).forceLoad();

                        return true;
                    }

                    return false;
                }


            });
            webView.loadUrl(kc.createLoginUrl());
            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();
        }
    }
}
