package net.lawaxi.li;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText text;
    ImageView image;
    TextView debug;
    public static final ArrayList<String> selectable = new ArrayList<>();
    public static final ArrayList<String> selectable_writer = new ArrayList<>();
    public static int index = 0;

    public static Runnable search;
    public static Runnable next;

    public static MainActivity ac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ac = this;
        text = (EditText) findViewById(R.id.editText);
        image = (ImageView) findViewById(R.id.imageView);
        debug = (TextView) findViewById(R.id.textView);

        search = new Runnable(){
            @Override
            public void run() {

                String out = getInputText(url.replace("0",toUnicode(text.getText().toString())));
                if(!out.equals("")) {
                    selectable.clear();
                    selectable_writer.clear();
                    index = 0;

                    for(int i=0;i<sequence.length;i++){
                        String delta = out;
                        int a = delta.indexOf(sequence[i]);
                        while(a>=80){
                            String b = delta.substring(a-80,a);
                            selectable.add(b.substring(b.indexOf("http"),b.indexOf("title=")-3));
                            selectable_writer.add(sequence[i]);
                            //System.out.println(b.substring(b.indexOf("http"),b.indexOf("title=")-3));
                            delta = delta.substring(a+5);
                            a = delta.indexOf(sequence[i]);
                        }
                    }

                    showPicture();
                }else
                    debug.setText("文字缺失");
            }
        };


        next = new Runnable() {
            @Override
            public void run() {
                showPicture();
            }
        };

        ((Button) findViewById(R.id.search)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(search).start(); }
        });

        ((Button) findViewById(R.id.next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(next).start(); }
        });

        image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                MediaStore.Images.Media.insertImage(getContentResolver(), ((BitmapDrawable)image.getDrawable()).getBitmap(), text.getText().toString(), getResources().getString(R.string.app_name));
                new AlertDialog.Builder(ac)
                        .setTitle(getResources().getString(R.string.app_name))
                        .setMessage("Saved successfully.")
                        .setPositiveButton("Fine", null)
                        .show();
            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            byte[] b = data.getByteArray("value");
            if(b!=null){
                image.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length));
            }
        }
    };

    private void showPicture(){
        if(!selectable.isEmpty()){
            debug.setText(selectable_writer.get(index%selectable.size()));
            byte[] b = getInputImage(selectable.get(index%selectable.size()));
            if(b==null){
                debug.setText("图片错误");
                return;
            }
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putByteArray("value", b);
            msg.setData(data);
            handler.sendMessage(msg);
            index++;
        }
    }

    private static final String url = "http://www.sfds.cn/0/4/";
    public static final String[] sequence = {"邓石如","何绍基","曹全碑","唐玄宗","吴让之","樊敏碑"};

    private static final String getInputText (String path){
        InputStream  inputStream = getInputStream(path);
        try {
            if(inputStream!=null)
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String s1,s2="";
                while(reader.read()!=-1) {
                    s1 = reader.readLine();
                    s2 += s1;
                }
                return s2;
            }
        }
        catch (Exception e){}
        return "";
    }

    private static final byte[] getInputImage (String path){
        InputStream inputStream = getInputStream(path);
        try {
            if (inputStream != null) {
                ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int n;
                while ((n = inputStream.read(buffer)) != -1) {
                    byteArrayOut.write(buffer, 0, n);
                }
                return byteArrayOut.toByteArray();
            }
        }
        catch (Exception e){}
        return null;
    }

    private static final InputStream getInputStream (String path) {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) (new URL(path)).openConnection();
            httpURLConnection.setConnectTimeout(3000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            System.out.println(httpURLConnection.getResponseCode());
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK)
                return httpURLConnection.getInputStream();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static final String toUnicode(String text) {
        char t = text.toCharArray()[0];
        return Integer.toString(t,16).toUpperCase();
    }

}
