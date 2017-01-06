package com.master.aluca.fitnessmd.webserver;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class ServerRequest {

    private static final String LOG_TAG="Fitness_ServerRequest";


    static JSONObject jObj = null;


    public ServerRequest() {

    }

    public JSONObject getJSONResponse(String url, List<NameValuePair> params) {
        Log.d(LOG_TAG, "getJSONResponse");

        InputStream inputStream = null;
        String json = "";
        JSONObject oRet = null;
        int responseStatusCode = -1;

        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse httpResponse = httpClient.execute(httpPost);

            responseStatusCode = httpResponse.getStatusLine().getStatusCode();
            Log.d(LOG_TAG, "responseStatusCode" + responseStatusCode);
            switch (responseStatusCode) {
                case 201 :
                    Log.d(LOG_TAG, "Status 201 : Accepted");
                    break;
                case 400:
                    Log.d(LOG_TAG, "Status 400 : Bad request");
                    break;
                case 401 :
                    Log.d(LOG_TAG,"Status 401 : Unauthorized");
                case 404 :
                    Log.d(LOG_TAG, "Status 404 : Not found");
                    break;
            }
            HttpEntity httpEntity = httpResponse.getEntity();
            inputStream = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG,"responseStatusCode : " + responseStatusCode);
        if (responseStatusCode == 201 && responseStatusCode != -1) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        inputStream, "iso-8859-1"), 8);
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "n");
                }
                inputStream.close();
                json = stringBuilder.toString();
            } catch (Exception e) {
                Log.e("Buffer Error", "Error converting result " + e.toString());
            }

            try {
                oRet = new JSONObject(json);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error parsing data " + e.toString());
                e.printStackTrace();
            }
        } else if (responseStatusCode == 400) {
            oRet = new JSONObject();
            try {
                oRet.put("message",400);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (responseStatusCode == 401) {
            oRet = new JSONObject();
            try {
                oRet.put("message",401);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (responseStatusCode == 404) {
            oRet = new JSONObject();
            try {
                oRet.put("message",404);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        return oRet;

    }

    public JSONObject requestLogin(String url, List<NameValuePair> params) {
        Log.d(LOG_TAG, "requestLogin url : " + url + " >>> params : " + params.get(0));
        JSONObject jobj = null;
        Params param = new Params(url,params);
        Request myTask = new Request();
        try{
            jobj= myTask.execute(param).get();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }catch (ExecutionException e){
            e.printStackTrace();
        }
        return jobj;
    }

    public JSONObject putPedometerData(String url, List<NameValuePair> params) {
        Log.d(LOG_TAG, "putPedometerData url : " + url + " >>> params : " + params.get(0));
        JSONObject jobj = null;
        Params param = new Params(url,params);
        Request myTask = new Request();
        try{
            jobj= myTask.execute(param).get();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }catch (ExecutionException e){
            e.printStackTrace();
        }
        return jobj;
    }

    public JSONObject requestWeight(String url, List<NameValuePair> params) {
        Log.d(LOG_TAG, "requestLogin url : " + url + " >>> params : " + params.get(0));
        JSONObject jobj = null;
        Params param = new Params(url,params);
        Request myTask = new Request();
        try{
            jobj= myTask.execute(param).get();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }catch (ExecutionException e){
            e.printStackTrace();
        }
        return jobj;
    }


    private static class Params {
        String url;
        List<NameValuePair> params;

        Params(String url, List<NameValuePair> params) {
            this.url = url;
            this.params = params;
        }
    }

    private class Request extends AsyncTask<Params, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(Params... args) {
            Log.d(LOG_TAG, "Request doInBackground");
            JSONObject json = getJSONResponse(args[0].url,args[0].params);

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            Log.d(LOG_TAG, "Request onPostExecute : " + json.toString());
            super.onPostExecute(json);

        }
    }
}