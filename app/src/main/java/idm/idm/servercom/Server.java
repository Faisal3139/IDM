package idm.idm.servercom;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.OkHttpClient;
import okhttp3.*;

/**
 * Created by Lily
 * Used to make requests to a server.
 */

public class Server {

    public static final Server SERVER = new Server();

    private final String ADDRESS = "http://3.128.46.46/";
    private URL url;
    private static String session_cookie;

    private static String status;
    private String status1;
    private String message;

    public boolean login(String username, String password)
    {
        try {
            new LoginRequest().execute(username, password).get();
            if(status.contains("200"))
                return true;
        }
        catch(Exception exc)
        {
            System.out.println(exc.getMessage());
        }
        return false;
    }

    public void register(JSONObject postData) {
        try {
            new registerRequest().execute(postData.toString());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private class registerRequest extends AsyncTask<String, Integer, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            try {
                registerTask(strings[0]);
            }
            catch(JSONException e) {
                System.out.println(e.getMessage());
                System.exit(1);
            }
            return null;
        }
    }

    private class LoginRequest extends AsyncTask<String, Integer, JSONObject>
    {

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
        }
        @Override
        protected JSONObject doInBackground(String... strings) {
            try {
                loginTask(strings[0], strings[1]);
            }
            catch(JSONException jsonexc)
            {
                System.out.println(jsonexc.getMessage());
                System.exit(1);
            }
            return null;
        }
    }

    private void loginTask (String username, String password) throws JSONException {

        try {

            JSONObject loginData = new JSONObject();
            loginData.put("username", username);
            loginData.put("password", password);

            //Creating Objects
            url = new URL(ADDRESS+"login");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            Log.d(con.toString(), "HttpURLConnection established...");

            //set up connection
            con.setRequestMethod("POST");
            //con.setDoOutput(true); //don't include this in POST request
            con.setRequestProperty("Content-Type", "application/json");

            DataOutputStream outputStream = new DataOutputStream(con.getOutputStream());
            outputStream.writeBytes(loginData.toString());
            outputStream.flush();
            outputStream.close();

            Log.d(loginData.toString(), "LOGIN DATA"); //debug

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            System.out.println("Got inputStream: " + in);
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println("Response: " + response.toString());

            status = response.toString();

            JSONObject jsonObj = new JSONObject(status);
            //String cookie = jsonObj.getString("message");
            session_cookie = jsonObj.getString("message");
            System.out.println(session_cookie); //debug

            con.disconnect();

        }

        catch(IOException exc)
        {
            System.out.println(exc.getMessage());
        }
    }

    public void UploadTask(File path)
    {
        try {
            new UploadTaskAsync().execute(path).get();
        }
        catch(Exception exc)
        {
            System.out.println(exc.getMessage());
        }
    }

    private class UploadTaskAsync extends AsyncTask<File, Integer, JSONObject>
    {

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected JSONObject doInBackground(File... files) {
            try {
                UploadTaskMethod(files[0]);
            }
            catch(JSONException jsonexc)
            {
                System.out.println(jsonexc.getMessage());
                System.exit(1);
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
        }
    }



    public void UploadTaskMethod(File path) throws JSONException {
        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("face",path.getName(),
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    new File(path.getPath())))
                    .build();
            Request request = new Request.Builder()
                    .url("http://3.128.46.46/upload")
                    .method("POST", body)
                    .addHeader("authorization", session_cookie)
                    .build();
            Response response = client.newCall(request).execute();
            System.out.println(response);
        }

        catch (IOException exc ) {
            System.out.println(exc.getMessage());
        }
    }

    public URLConnection openConnection() throws IOException {
        throw new RuntimeException("Stub");
    }

    private void registerTask (String JSON) throws JSONException {

        try {

            Log.i("JSON", JSON);

            url = new URL("http://3.128.46.46/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(JSON);
            wr.flush();
            wr.close();

            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("MSG" , conn.getResponseMessage());

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
