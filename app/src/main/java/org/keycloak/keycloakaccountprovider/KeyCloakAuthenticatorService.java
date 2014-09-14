package org.keycloak.keycloakaccountprovider;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KeyCloakAuthenticatorService extends Service {

    private KeyCloakAccountAuthenticator authenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        authenticator = new KeyCloakAccountAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
