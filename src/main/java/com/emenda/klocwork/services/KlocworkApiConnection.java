package com.emenda.klocwork.services;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.ConnectException;
import java.lang.String;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.net.ConnectException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class KlocworkApiConnection {

	private URL url;
    private String user;
    private String ltoken;

    /*
	 * Argument constructors
	 */
	public KlocworkApiConnection(String url, String user, String ltoken) throws IOException {
        this.url = new URL(url + "/review/api");
        this.user = user;
        this.ltoken = ltoken;
	}

	/*
	 * Function to connect to server using member variables host and connection
	 */
	private HttpURLConnection createConnection() throws IOException, KeyManagementException, NoSuchAlgorithmException {
        HttpURLConnection httpUrlConnection;
        if(url.getProtocol().equals("https")) {
            httpUrlConnection = (HttpsURLConnection) url.openConnection();
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
                public X509Certificate[] getAcceptedIssuers(){return null;}
                public void checkClientTrusted(X509Certificate[] certs, String authType){}
                public void checkServerTrusted(X509Certificate[] certs, String authType){}
            }};
            // Install the trust manager for SSL use
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            ((HttpsURLConnection) httpUrlConnection).setSSLSocketFactory(sc.getSocketFactory());
        } else {
            httpUrlConnection = (HttpURLConnection) url.openConnection();
        }
        // Settings for the connection
        httpUrlConnection.setDoOutput(true);
        httpUrlConnection.setDoInput(true);
        httpUrlConnection.setInstanceFollowRedirects(false);
        // Set the request method to POST (accepted by KW Web API)
        httpUrlConnection.setRequestMethod("POST");
        httpUrlConnection.setUseCaches(false);

        return httpUrlConnection;
	}

	public JSONArray sendRequest(String request) throws IOException {

        JSONArray response = new JSONArray();
        String errorMsg = "";

        request += "&user=" + user;
        request += "&ltoken=" + ltoken;
        boolean success = false;
        try {
            HttpURLConnection httpUrlConnection = createConnection();
            httpUrlConnection.setRequestProperty("Content-Length", Integer.toString(request.length()));

    		// Write the request to the connection
    		try {
                DataOutputStream wr = new DataOutputStream(httpUrlConnection.getOutputStream());
    			wr.writeBytes(request);
    			// Close the streams
    			wr.flush();
    			wr.close();

                InputStream inputStream = httpUrlConnection.getErrorStream();
                if (inputStream == null) {
                    success = true;
                    inputStream = httpUrlConnection.getInputStream();
                }

    			BufferedReader buf = new BufferedReader(new InputStreamReader(
                            httpUrlConnection.getInputStream(),"UTF-8"));

                String line;
                while (null != (line = buf.readLine())) {
                    if (success) {
                        response.add(JSONObject.fromObject(line));
                    } else {
                        errorMsg += line + "\n";
                    }
    			}
    			// Close the streams
    			buf.close();
    			inputStream.close();
    		} finally {
                // always close the HttpUrlConnection and propagate any exceptions
                // to the caller to handle
                if(httpUrlConnection != null) {
        			httpUrlConnection.disconnect();
        		}
        		httpUrlConnection = null;
            }
        } catch (KeyManagementException | NoSuchAlgorithmException | ConnectException ex) {
            // TODO: should not catch "Exception", bad practice, but when server
            // returns http code 500 (internal error) which exception is thrown?
            // must be subclass of IOException
            throw new IOException(
                "Error: connection to Klocwork Server \"" +
                url.toString() + "\" failed.\n" +
                "Request: " + request + "\n" +
                "Cause: " + ex.getMessage(), ex);
        }
        if (!success) {
            throw new IOException(
                "Error: request was not successfully handled by Klocwork server \"" +
                url.toString() + "\".\n" +
                "Request: " + request + "\n" +
                "Return: " + errorMsg
            );
        }
        return response;
	}


}
