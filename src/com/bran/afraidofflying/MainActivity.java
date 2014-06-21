package com.bran.afraidofflying;

import java.util.ArrayList;
import java.util.Random;

import com.bran.afraidofflying.R;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {
	private static final String TAG = "Afraid of Flying";
	private static final String DEVICE_ID_KEY = "deviceId";
	private static final int ACCELEROMETER_LOW_PASS_FILTER_BUFFER_LENGTH = 4;
	private static final int MARGIN_OF_ERROR = 4 * ACCELEROMETER_LOW_PASS_FILTER_BUFFER_LENGTH;//distance from 0 still registered as free fall
	protected static final float STATIONARY = 13 * ACCELEROMETER_LOW_PASS_FILTER_BUFFER_LENGTH;// Normal sum of accelerometer components while phone at rest
	private static final int GYROSCOPE_LOW_PASS_FILTER_BUFFER_LENGTH = 4;
	private static final float ROTATION_THROW_THRESHOLD = 25 * GYROSCOPE_LOW_PASS_FILTER_BUFFER_LENGTH;
	private static final String ACCEPTED_EULA_TAG = "acceptedEula";
	private DataFilter filter, rotFilter;
	private Boolean alreadyScreamedThisThrow;
	private SensorManager manager;
	private Sensor accelerometer;
	private Sensor gyro;
	private Boolean hasGyroscope;
	private Screamer screamer;
	private Random numGenerator;
	private ImageView head, mouth, eyes;
	private int[] heads, mouths, eyetypes;
	private TextView text;
	private int[] texts;
	private Boolean  topEmpty;
	
	private float deviceId;
	private long startTime;
	private int screamCount;
	
	ArrayList<Float> rotReadings = new ArrayList<Float>(500);
	ArrayList<Float> readings = new ArrayList<Float>(500);
	Button btn;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Parse.initialize(this, "TTjVV1rjK0qiXqEakIBiLk94ByLNoEUp62539HNl", "RQFsoaWZAyj5p4HuhVGjjwtuo13aMJDDAScE0gAA");
		
		new AppEULA(this).show();
		prepVolume();
		initVariables();
		
		ParseAnalytics.trackAppOpened(getIntent());
	}
	
	 protected void onResume() {
       super.onResume();
       Log.d(TAG, "onResume()");
       screamer = new Screamer(this);
       prepAnalyticsData();
       prepSensors();
       manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
       manager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
   }

   protected void onPause() {
       super.onPause();
       Log.d(TAG, "onPause()");
       transferAnalyticsData();//transfer data to Parse
       manager.unregisterListener(this);
		screamer.release();//Release media player
		Editor data = getPreferences(MODE_PRIVATE).edit();
		data.putFloat(DEVICE_ID_KEY, deviceId);
		data.commit();
   }

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			float term = filter.lowPassFilter(event.values);
			readings.add(term);
			if(!alreadyScreamedThisThrow && term<MARGIN_OF_ERROR) { //thrown
					scream();
			}
			if(Math.abs(term - STATIONARY) < MARGIN_OF_ERROR){ //held stationary
				alreadyScreamedThisThrow = false;
			}
		}
		else if(hasGyroscope){
			float rot = rotFilter.lowPassFilter(event.values);
			rotReadings.add(rot);
			if(!alreadyScreamedThisThrow && rot > ROTATION_THROW_THRESHOLD){
				scream();
			}
		}
	}
	
	private void transferAnalyticsData(){
		ParseObject session = new ParseObject("SessionV2");
		session.put("deviceId",  deviceId);
		session.put("timeOpen", (System.currentTimeMillis() - startTime)/1000);
		session.put("marginalScreamCount", screamCount);
		session.saveEventually();
		Log.i(TAG, "Analytics Data sent to Parse.com");
	}

	private void prepSensors() {
		manager = (SensorManager)getSystemService(SENSOR_SERVICE);
		accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		gyro = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		hasGyroscope = null!=gyro;
		Log.d(TAG, "gyroscope: " + hasGyroscope);
		if(null == accelerometer) finish();
	}
	
	private void prepAnalyticsData() {
		SharedPreferences savedData = getPreferences(MODE_PRIVATE); 
		deviceId = savedData.getFloat(DEVICE_ID_KEY, (float)numGenerator.nextDouble());
		startTime = System.currentTimeMillis();
		screamCount = 0;
	}

	private void initVariables() {
		alreadyScreamedThisThrow = true;// to prevent scream at time=0
		topEmpty = false;
		numGenerator = new Random();
		filter = new DataFilter(ACCELEROMETER_LOW_PASS_FILTER_BUFFER_LENGTH);
		rotFilter = new DataFilter(GYROSCOPE_LOW_PASS_FILTER_BUFFER_LENGTH);
		head = (ImageView) findViewById(R.id.head);
		heads = new int[]{
				R.drawable.blue,
				R.drawable.blue2,
				R.drawable.blue3,
				R.drawable.green,
				R.drawable.green2,
				R.drawable.green3,
				R.drawable.purple,
				R.drawable.purple2,
				R.drawable.purple3,
				R.drawable.red,
				R.drawable.red2,
				R.drawable.yellow,
				R.drawable.yellow2,
				R.drawable.yellow3
		};
		mouth = (ImageView) findViewById(R.id.mouth);
		mouths = new int[]{
				R.drawable.curvy_mouth,
				R.drawable.extra_curvy_mouth,
				R.drawable.jagged_mouth,
				R.drawable.slant_mouth,
				R.drawable.straight_mouth
		};
		eyes = (ImageView) findViewById(R.id.eyes);
		eyetypes = new int[]{
				R.drawable.normal_eyes,
				R.drawable.red_normal_eyes,
				R.drawable.crazy_eyes,
				R.drawable.crazy_eyes2,
				R.drawable.crazy_eyes3,
				R.drawable.crazy_eyes4,
				R.drawable.crazy_eyes5,
				R.drawable.empty_eyes,
				R.drawable.target_eyes
		};
		text = (TextView) findViewById(R.id.bottom_text);
		texts = new int[]{
			R.string.text1,
			R.string.text2,
			R.string.text3,
			R.string.text4,
			R.string.text5,
			R.string.text6,
			R.string.text7,
			R.string.text8,
			R.string.text9,
			R.string.text10,
			R.string.text11,
			R.string.text12,
			R.string.text13,
			R.string.text14,
			R.string.text15,
			R.string.text16,
			R.string.text17,
			R.string.text18,
			R.string.text19,
			R.string.text20
		};
	}

	private void scream() {
		screamer.scream();
		changeFace();
		changeText();
		if(!topEmpty){
			((TextView) findViewById(R.id.top_text)).setText("\n");
			topEmpty = true;
		}
		screamCount++;
		alreadyScreamedThisThrow = true;
	}

	private void changeFace() {
		head.setImageResource(heads[(int) Math.floor(numGenerator.nextDouble()*heads.length)]);
		mouth.setImageResource(mouths[(int) Math.floor(numGenerator.nextDouble()*mouths.length)]);
		eyes.setImageResource(eyetypes[(int) Math.floor(numGenerator.nextDouble()*eyetypes.length)]);
	}

	private void changeText() {
		int index = (int) Math.floor(numGenerator.nextDouble()*texts.length);
		text.setText(getString(texts[index]));
	}
	
    protected void onDestroy(){ 
    	super.onDestroy();
    	Log.d(TAG, "onDestroy()"); }
    
	protected void onStop(){//onDestroy?
		super.onStop();
		Log.d(TAG, "onStop()");
	}

	private void prepVolume() {
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		checkNotifyVolumeDown();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.instructions:
	    	showExplanation();
	        return true;
	    case R.id.rate_us:
	    	Uri uri = Uri.parse("market://details?id=com.ran.benjamin.afraidofflying");
	    	startActivity(new Intent (Intent.ACTION_VIEW, uri));
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	private void showExplanation() {//Reverts text on screen to initial hints about how to use app
		((TextView) findViewById(R.id.top_text)).setText(getString(R.string.starter_top_text));
		text.setText(getString(R.string.starter_bottom_text));
		topEmpty = false;
		checkNotifyVolumeDown();
	}

	private void checkNotifyVolumeDown() {
		AudioManager audioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
		if(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0)Toast.makeText(getApplicationContext(), "You may want to raise your volume.", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
		
	}
}
