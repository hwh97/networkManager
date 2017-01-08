package cn.hwwwwh.networkmanager;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.loopj.android.http.HttpGet;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;


public class MainActivity extends AppCompatActivity {
    private Switch aswitch;
    public Timer timer;
    private TimerTask timerTask;
    private EditText editText;
    private int Count;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private RadioButton radioButton1;
    private RadioButton radioButton2;
    private RadioButton radioButton3;
    private RadioButton radioButton4;
    private CheckBox checkBox1;
    private CheckBox checkBox2;
    private CheckBox checkBox3;
    private Toast toast;
    private Vibrator vibrator;
    private MediaPlayer mp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intiView();
        radioButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText.getText().length()==0){
                    Toast.makeText(MainActivity.this,"请输入监测数值",Toast.LENGTH_SHORT).show();
                }
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
                //没有按钮选中且
                if (editText.getText().length() != 0 ) {
                    radioButton4.setEnabled(true);
                    radioButton4.setChecked(true);
                } else{
                    radioButton4.setEnabled(false);
                    radioButton1.setChecked(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        aswitch=(Switch)findViewById(R.id.switch1);
        aswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (radioGroup.getCheckedRadioButtonId() !=R.id.radioButton4) {
                        radioButton = (RadioButton) findViewById(radioGroup.getCheckedRadioButtonId());
                        Count = Integer.parseInt(radioButton.getText().toString());
                    }
                    else {
                        Count = Integer.parseInt(editText.getText().toString());
                    }
                    radioButton1.setEnabled(false);
                    radioButton2.setEnabled(false);
                    radioButton3.setEnabled(false);
                    radioButton4.setEnabled(false);
                    editText.setEnabled(false);
                    setTimer(Count);
                } else {
                    radioButton1.setEnabled(true);
                    radioButton2.setEnabled(true);
                    radioButton3.setEnabled(true);
                    if(editText.getText().length()==0){
                        radioButton4.setEnabled(false);
                    }else{
                        radioButton4.setEnabled(true);
                    }
                    editText.setEnabled(true);
                    cancelTimer();
                    cancelToast();
                    if(vibrator !=null) {
                        vibrator.cancel();
                    }
                    if(mp!=null) {
                        mp.stop();
                    }
                }
            }
        });
    }
    private void setTimer(int count) {
        if (timer != null) {
            timer.cancel();
            timer=null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask=null;
        }

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (HttpUtils.isNetConn(MainActivity.this)) {
                    new myAsyncTask(MainActivity.this).execute("http://cs.hwwwwh.cn/request.php");
                }
            }
        };
        timer.schedule(timerTask, 1000, count * 1000);
    }

    private void cancelTimer(){
        if (timer != null) {
            timer.cancel();
            timer=null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask=null;
        }
    }

    private void intiView(){
        editText=(EditText)findViewById(R.id.inputTime);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioButton1=(RadioButton)findViewById(R.id.radioButton);
        radioButton2=(RadioButton)findViewById(R.id.radioButton2);
        radioButton3=(RadioButton)findViewById(R.id.radioButton3);
        radioButton4=(RadioButton)findViewById(R.id.radioButton4);
        checkBox1=(CheckBox)findViewById(R.id.checkBox);
        checkBox2=(CheckBox)findViewById(R.id.checkBox2);
        checkBox3=(CheckBox)findViewById(R.id.checkBox3);
        vibrator=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void showToast(String text) {
        if(toast == null) {
            toast = Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT);
        } else {
            toast.setText(text);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    public void cancelToast() {
        if (toast != null) {
            toast.cancel();
        }
    }

    public boolean getCheckBox(CheckBox checkBox){
        if(checkBox!=null){
            return checkBox.isChecked();
        }
        return false;
    }

    public void openVibrator(){
        long[] pattern={100,400};
        vibrator.vibrate(pattern,-1);
    }

    public void playMedia(){
        mp=new MediaPlayer();
        try {
            mp.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            mp.prepare();
            mp.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onStop(){
        super.onStop();
        vibrator.cancel();
        mp.stop();
    }

    class myAsyncTask extends AsyncTask<String,Boolean,Boolean>{
        private MainActivity activity;
        public myAsyncTask(Activity activity){
            this.activity=(MainActivity)activity;
        }
        //耗时操作
        @Override
        protected Boolean doInBackground(String... params) {
            Boolean isNetworkConnect=false;
            try{
                byte[] b=HttpUtils.downloadFromNet(params[0]);
                String jsonString=new String(b);
                isNetworkConnect=Boolean.parseBoolean(jsonString);
                return isNetworkConnect;
                }catch (Exception e){

                }
            return isNetworkConnect;
        }
        //更新UI
        @Override
        protected void onProgressUpdate(Boolean... values) {
            super.onProgressUpdate(values);
        }
        //完成后
        @Override
        protected void onPostExecute(Boolean s) {
            Log.d("Tag", s.toString());
            if(s){
                if(getCheckBox(checkBox1)){
                    showToast("网络连接成功");
                }
                if(getCheckBox(checkBox2)){
                    openVibrator();
                }
                if (getCheckBox(checkBox3)) {
                    playMedia();
                }
            }
            super.onPostExecute(s);
        }
    }
}
