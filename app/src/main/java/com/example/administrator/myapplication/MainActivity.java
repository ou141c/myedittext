package com.example.administrator.myapplication;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.administrator.myapplication.utils.BitmapUtils;
import com.example.administrator.myapplication.utils.CompressUtil;
import com.example.administrator.myapplication.utils.FileUtils;

import net.lingala.zip4j.exception.ZipException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {
    /**
     * Called when the activity is first created.
     */
    private Button mBtnInsertn, mBtnSave, mBtnRead;
    private Button mBtnUnZip,mBtnZip;
    private EditText mEText;
    private LinearLayout mLltContent;
    /**
     * 相机返回的Bitmap
     */
    private Bitmap mBitmapResult;
    private static final int PHOTO_SUCCESS = 1;
    private static final int CAMERA_SUCCESS = 2;

    private static String full_name = "";
    private static final String LAST_NAME = "img_";
    private int first_name = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        mBtnZip = (Button) findViewById(R.id.btn_edit_zip);
        mBtnUnZip = (Button) findViewById(R.id.btn_edit_unzip);
        mLltContent = (LinearLayout) findViewById(R.id.llt_content);
        mBtnInsertn = (Button) findViewById(R.id.btn_edit_insertn);
        mBtnSave = (Button) findViewById(R.id.btn_edit_save);
        mBtnRead = (Button) findViewById(R.id.btn_edit_read);
        mEText = (EditText) findViewById(R.id.myEdit);

        /***
         * 给控件增加截取当前图片功能
         */
        mEText.setDrawingCacheEnabled(true);
        mEText.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        mEText.layout(0, 0, mEText.getMeasuredWidth(),
        mEText.getMeasuredHeight());
        mEText.buildDrawingCache();

        mBtnInsertn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
                getImage.addCategory(Intent.CATEGORY_OPENABLE);
                getImage.setType("image/*");
                startActivityForResult(getImage, PHOTO_SUCCESS);

/*                    final CharSequence[] items = { "手机相册", "相机拍摄" };
                    AlertDialog dlg = new AlertDialog.Builder(MainActivity.this).setTitle("选择图片").setItems(items,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int item) {
                                    //这里item是根据选择的方式,
                                    //在items数组里面定义了两种方式, 拍照的下标为1所以就调用拍照方法
                                    if(item==1){
                                        Intent getImageByCamera= new Intent("android.media.action.IMAGE_CAPTURE");
                                        startActivityForResult(getImageByCamera, CAMERA_SUCCESS);
                                    }else{
                                        Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
                                        getImage.addCategory(Intent.CATEGORY_OPENABLE);
                                        getImage.setType("image*//*");
                                        startActivityForResult(getImage, PHOTO_SUCCESS);
                                    }
                                }
                            }).create();
                    dlg.show();*/
//                e.insertDrawable(R.drawable.easy);
            }
        });
        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileUtils.saveTextSd(mEText.getText().toString(), "content");
                mEText.setText("");
            }
        });
        mBtnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt = FileUtils.readTextWrapSd();
//                    mEText.setText(txt);


                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(txt);
                Pattern pattern = Pattern.compile("\\[p](\\S+?)\\[/p]");//匹配[xx]的字符串
                Matcher matcher = pattern.matcher(txt);
                while (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    String group = matcher.group();
                    group = group.substring(3, group.length() - 4);
                    FileInputStream fis = null;
                    Bitmap bitmap = FileUtils.readBitmapSd("/sdcard/niannian/002/" + group + ".png");
                    ImageSpan imageSpan = new ImageSpan(MainActivity.this, bitmap);
                    spannableStringBuilder.setSpan(imageSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    BitmapUtils.recycled(bitmap);
                }
                mEText.setText(spannableStringBuilder);
                mEText.setSelection(spannableStringBuilder.length());
                //捕获当前VIEW的内容,并保存
//                Bitmap bit = mEText.getDrawingCache();
//                FileUtils.saveBitmapSd(bit, "5555555555");
            }
        });

        mBtnZip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CompressUtil.zip("/sdcard/niannian/002/");
            }
        });
        mBtnUnZip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    CompressUtil.unzip("/sdcard/niannian/002.zip","/sdcard/niannian/003",null);
                } catch (ZipException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        ContentResolver resolver = getContentResolver();
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTO_SUCCESS:
                    //获得图片的uri
                    Uri originalUri = intent.getData();
                    mBitmapResult = null;
                    try {
                        Bitmap originalBitmap = BitmapFactory.decodeStream(resolver.openInputStream(originalUri));
                        mBitmapResult = BitmapUtils.resizeImage(originalBitmap, 80, 80);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (mBitmapResult != null) {
                        //根据Bitmap对象创建ImageSpan对象
                        ImageSpan imageSpan = new ImageSpan(MainActivity.this, mBitmapResult);
                        //创建一个SpannableString对象，以便插入用ImageSpan对象封装的图像
                        full_name = LAST_NAME + first_name;
                        String s = "[p]" + full_name + "[/p]";
                        first_name++;
                        SpannableString spannableString = new SpannableString(s);
                        //  用ImageSpan对象替换face
                        spannableString.setSpan(imageSpan, 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        //将选择的图片追加到EditText中光标所在位置
                        int index = mEText.getSelectionStart(); //获取光标所在位置
                        Editable edit_text = mEText.getEditableText();
                        if (index < 0 || index >= edit_text.length()) {
                            edit_text.append(spannableString);
                        } else {
                            edit_text.insert(index, spannableString);
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                FileUtils.saveBitmapSd(mBitmapResult, full_name);
                            }
                        }).start();
                    } else {
                        Toast.makeText(MainActivity.this, "获取图片失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case CAMERA_SUCCESS:
                    Bundle extras = intent.getExtras();
                    Bitmap originalBitmap1 = (Bitmap) extras.get("data");
                    if (originalBitmap1 != null) {
                        mBitmapResult = BitmapUtils.resizeImage(originalBitmap1, 200, 200);
                        //根据Bitmap对象创建ImageSpan对象
                        ImageSpan imageSpan = new ImageSpan(MainActivity.this, mBitmapResult);
                        //创建一个SpannableString对象，以便插入用ImageSpan对象封装的图像
                        SpannableString spannableString = new SpannableString("[local]" + 1 + "[/local]");
                        //  用ImageSpan对象替换face
                        spannableString.setSpan(imageSpan, 0, "[local]1[local]".length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        //将选择的图片追加到EditText中光标所在位置
                        int index = mEText.getSelectionStart(); //获取光标所在位置
                        Editable edit_text = mEText.getEditableText();
                        if (index < 0 || index >= edit_text.length()) {
                            edit_text.append(spannableString);
                        } else {
                            edit_text.insert(index, spannableString);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "获取图片失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
