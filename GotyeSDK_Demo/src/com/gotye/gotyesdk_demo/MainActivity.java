package com.gotye.gotyesdk_demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.gotye.api.Gotye;
import com.gotye.api.bean.GotyeRoom;
import com.gotye.api.bean.GotyeSex;
import com.gotye.gotyesdk.demo.R;
import com.gotye.sdk.GotyeSDK;

public class MainActivity extends Activity {
	
	String[] nickname = new String[]{"德玛西亚", "天下无敌", "阿西吧", "努力的小弟", "沙龙巴斯", "辛巴！"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final EditText ipEdit = (EditText) findViewById(R.id.ip);
		final EditText portEdit = (EditText) findViewById(R.id.port);
		
		final EditText usernameEdit = (EditText) findViewById(R.id.username);
		final EditText nickNameEdit = (EditText) findViewById(R.id.nickname);
		final RadioGroup sexGroup = (RadioGroup) findViewById(R.id.sex_group);
		
		findViewById(R.id.enterRoom).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				//设置登录ip及端口，这里是demo所以写在这里方便测试，最好写在application类中，保证程序启动就初始化，要不然可能出现没有设置ip而登不上的情况
				try{
					if(!TextUtils.isEmpty(ipEdit.getText().toString())){
						Gotye.getInstance().setLoginIP(ipEdit.getText().toString());
					}else {
//						Gotye.getInstance().setLoginIP("42.62.40.156");
					}
				}catch(Exception e){
//					Gotye.getInstance().setLoginIP("42.62.40.156");
				}
//				Gotye.getInstance().setLoginIP("192.168.1.11");
				try{
					Gotye.getInstance().setLoginPort(Integer.parseInt(portEdit.getText().toString()));
				}catch(Exception e){
//					Gotye.getInstance().setLoginPort(38888);
				}
				
				EditText roomName = (EditText) findViewById(R.id.roomName);
				EditText roomID = (EditText) findViewById(R.id.roomID);
				long roomIDF = 0;
				try{
					roomIDF = Long.valueOf(roomID.getText().toString());
				}catch(Exception e){
					return;
				}
				
				
				String username = usernameEdit.getText().toString();

				if (username.length() > 40) {
					Toast.makeText(MainActivity.this, "账号太长或为空",
							Toast.LENGTH_SHORT).show();
					return;
				}else if(TextUtils.isEmpty(username)){
					username = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);	
				}

				String nickName = nickNameEdit.getText().toString();

				GotyeSex sex = GotyeSex.MAN;
				int id = sexGroup.getCheckedRadioButtonId();
				switch (id) {
				case R.id.sex_female:
					sex = GotyeSex.WOMEN;
					break;
				case R.id.sex_male:
					sex = GotyeSex.MAN;
					break;
				case R.id.sex_notset:
					sex = GotyeSex.NOT_SET;
					break;

				default:
					sex = GotyeSex.NOT_SET;
					break;
				}
				
				Bitmap head = BitmapFactory.decodeResource(getResources(),
						R.drawable.gotye_head_demo);
				
				Bundle bundle = new Bundle();
				GotyeRoom room = new GotyeRoom(roomIDF);
				room.setRoomName(roomName.getText().toString() + "");
				bundle.putSerializable(GotyeSDK.DIRECT_ENTER_ROOM, room);
				GotyeSDK.getInstance().startGotyeSDK(MainActivity.this, username, nickName, sex, head, bundle);
				
			}
		});
		
		findViewById(R.id.start).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				//设置登录ip及端口，这里是demo所以写在这里方便测试，最好写在application类中，保证程序启动就初始化，要不然可能出现没有设置ip而登不上的情况
				try{
					if(!TextUtils.isEmpty(ipEdit.getText().toString())){
						Gotye.getInstance().setLoginIP(ipEdit.getText().toString());
					}else {
//						Gotye.getInstance().setLoginIP("42.62.40.156");
					}
				}catch(Exception e){
//					Gotye.getInstance().setLoginIP("42.62.40.156");
				}
//				Gotye.getInstance().setLoginIP("192.168.1.11");
				try{
					Gotye.getInstance().setLoginPort(Integer.parseInt(portEdit.getText().toString()));
				}catch(Exception e){
//					Gotye.getInstance().setLoginPort(38888);
				}
				
				
				String username = usernameEdit.getText().toString();

				if (username.length() > 40) {
					Toast.makeText(MainActivity.this, "账号太长或为空",
							Toast.LENGTH_SHORT).show();
					return;
				}else if(TextUtils.isEmpty(username)){
					username = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);	
				}

				String nickName = nickNameEdit.getText().toString();

				GotyeSex sex = GotyeSex.MAN;
				int id = sexGroup.getCheckedRadioButtonId();
				switch (id) {
				case R.id.sex_female:
					sex = GotyeSex.WOMEN;
					break;
				case R.id.sex_male:
					sex = GotyeSex.MAN;
					break;
				case R.id.sex_notset:
					sex = GotyeSex.NOT_SET;
					break;

				default:
					sex = GotyeSex.NOT_SET;
					break;
				}
				Bitmap head = BitmapFactory.decodeResource(getResources(),
						R.drawable.gotye_head_demo);
				
				GotyeSDK.getInstance().startGotyeSDK(MainActivity.this, username, nickName, sex, head, null);
				
			}
		});
		
		nickNameEdit.setText(nickname[(int) (Math.random() * (nickname.length - 1))]);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public static Bitmap toRoundCorner(Context context, Bitmap src, Bitmap dst) {
		
		NinePatchDrawable nine = (NinePatchDrawable) context.getResources().getDrawable(R.drawable.gotye_bg_msg_text_normal_right);
		nine.setBounds(0, 0, src.getWidth(), src.getHeight());
		
		Bitmap output = Bitmap.createBitmap(src.getWidth(),
				src.getHeight(), Config.ARGB_8888);
		
		Canvas canvas = new Canvas(output);
		nine.draw(canvas);
		
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		canvas.drawBitmap(dst, 0, 0, paint);
		
		paint.setXfermode(new PorterDuffXfermode(Mode.MULTIPLY));
		canvas.drawBitmap(src, 0, 0, paint);
		
		
		return output;
	}
}
