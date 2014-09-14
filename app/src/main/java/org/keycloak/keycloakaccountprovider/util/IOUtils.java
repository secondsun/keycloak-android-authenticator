package org.keycloak.keycloakaccountprovider.util;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class IOUtils {
    public static String getString(InputStream fileStream) {
        InputStreamReader inputreader = new InputStreamReader(fileStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while (( line = buffreader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

    public static void close(InputStream fileStream) {
        try {
            fileStream.close();
        } catch (IOException e) {
            Log.e("IOUtils", e.getMessage(), e);
        }
    }

    public static String encodeURIComponent(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String fetchToken(String url) {
        return fetchURLParam(url, "code");
    }
    public static String fetchError(String url) {
        return fetchURLParam(url, "error");
    }
    public static String fetchURLParam(String url, String param) {
        Uri uri = Uri.parse(url);
        return uri.getQueryParameter(param);
    }

}
