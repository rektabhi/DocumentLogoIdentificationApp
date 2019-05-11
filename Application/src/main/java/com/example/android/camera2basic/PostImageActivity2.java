package com.example.android.camera2basic;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
public class PostImageActivity2 extends AppCompatActivity{

        ProgressDialog pDialog;
        String image;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.post_image_activity);

            pDialog = new ProgressDialog(this);

            Button btn_upload = (Button) findViewById(R.id.btn_upload);
            btn_upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 0);
                }
            });

        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {

            if (data != null && requestCode == 0) {


                if (resultCode == RESULT_OK) {
                    Uri targetUri = data.getData();
                    Bitmap bitmap;
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                        //Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, false);
                        image = ConvertBitmapToString(bitmap);

                        Upload();

                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

        }

        //method to convert the selected image to base64 encoded string

        public static String ConvertBitmapToString(Bitmap bitmap){
            String encodedImage = "";

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            encodedImage= Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

            return encodedImage;
        }


        private void Upload() {

            try {
                new UploadFile().execute("http://192.168.225.157:5000/predict_image");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


        private class UploadFile extends AsyncTask<String, Void, Void> {


            private String Content;
            private String Error = null;
            String data;
            private BufferedReader reader;


            protected void onPreExecute() {

                pDialog.show();
                data = image;

            }

            protected Void doInBackground(String... urls) {

                HttpURLConnection connection = null;
                try {
                    URL url = new URL(urls[0]);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("POST");
                    con.setUseCaches(false);
                    con.setDoInput(true);
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Length", "" + data.getBytes().length);
                    con.setRequestProperty("Content-Type", "image/jpg");
                    con.setRequestProperty("Connection", "Keep-Alive");
                    con.setDoOutput(true);

                    OutputStream os = con.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                    //make request
                    writer.write(data);
                    Log.d(data, "Data data");
                    writer.flush();
                    writer.close();
                    reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }

                    Content = sb.toString();
                } catch (Exception ex) {
                    Error = ex.getMessage();
                }
                return null;

            }


            protected void onPostExecute(Void unused) {
                // NOTE: You can call UI Element here.

                pDialog.dismiss();
                try {

                    if (Content != null) {
                        JSONObject jsonResponse = new JSONObject(Content);
                        String status = jsonResponse.getString("status");
                        if ("200".equals(status)) {

                            Toast.makeText(getApplicationContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();

                        } else {

                            Toast.makeText(getApplicationContext(), "Something is wrong ! Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    }
