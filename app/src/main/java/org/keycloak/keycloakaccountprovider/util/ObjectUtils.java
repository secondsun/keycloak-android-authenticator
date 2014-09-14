package org.keycloak.keycloakaccountprovider.util;

import org.json.JSONObject;

/**
 * Created by Summers on 9/13/2014.
 */
public class ObjectUtils {
    public static <T>T getOrDefault(T mayBeNull, T defaultIfNull) {
        if (mayBeNull != null) {
            return mayBeNull;
        } else {
            return defaultIfNull;
        }
    }
}
