package yuantu.com.recordvideo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private MediaProjectionManager  mediaProjectionManager;
    //录制视频的工具
    private MediaProjection mediaProjection;
    private ScreenRecorder screenRecorder;
    int width,height,dpi;

    Thread thread;
    boolean isrun;
    Activity context;

    TextView startPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;
        startPlayer=(TextView) findViewById(R.id.binding);
        startPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isrun&&screenRecorder!=null){
                    stopRecord();
                }else {
                    startRecord();
                }
            }
        });

        mediaProjectionManager = (MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        width = outMetrics.widthPixels;
        height = outMetrics.heightPixels;
        dpi = outMetrics.densityDpi;

        getPermission();
    }

    private void getPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 102);
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 103);
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 104);
        }

        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            intent = mediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(intent, 101);//正常情况是要执行到这里的,作用是申请捕捉屏幕
        } else {
            Toast.makeText(context, "Android版本太低，无法使用该功能",Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 102) {
            Toast.makeText(context, "缺少读写权限", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == 103) {
            Toast.makeText(context, "缺少录音权限", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == 104) {
            Toast.makeText(context, "缺少相机权限", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode != 101) {
            Log.e("HandDrawActivity", "error requestCode =" + requestCode);
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(context, "捕捉屏幕被禁止", Toast.LENGTH_SHORT).show();
            return;
        }

        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);

        startRecord();

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startRecord() {
        if (mediaProjection != null) {
            screenRecorder = new ScreenRecorder(width, height, mediaProjection, dpi);
        }
        thread = new Thread() {
            @Override
            public void run() {
                screenRecorder.startRecorder();//跟ScreenRecorder有关的下文再说，总之这句话的意思就是开始录屏的意思
            }
        };
        thread.start();
        startPlayer.setText("停止");//开始和停止我用的同一个按钮，所以开始录屏之后把按钮文字改一下
        isrun = true;//录屏状态改成真
    }

    private void stopRecord(){
        screenRecorder.stop();
        startPlayer.setText("开始");
        isrun=false;
    }

    @Override
    protected void onDestroy() {
        if (screenRecorder!=null){
            screenRecorder.destory();
        }
        super.onDestroy();
    }
}
