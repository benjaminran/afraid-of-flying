package com.bran.afraidofflying;

import java.util.Random;
import com.bran.afraidofflying.R;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.util.SparseIntArray;

public class Screamer {
	private static final String TAG = "Afraid of Flying";
	private static SoundPool soundPool;
	private static SparseIntArray screams;
	private Random numGenerator;
	
	public Screamer(Context appCtx){
		Log.i(TAG, "constructor");
		soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
		screams = new SparseIntArray(23);    
		int counter = 0;
		for(int id : new int[]{ R.raw.scream, R.raw.scream2, R.raw.scream3, R.raw.scream4, 
				R.raw.scream5, R.raw.scream6, R.raw.scream7, R.raw.scream8, 
				R.raw.scream9, R.raw.scream10, R.raw.scream11, R.raw.scream12, 
				R.raw.scream13, R.raw.scream14, R.raw.scream15, R.raw.scream16,
				R.raw.scream17, R.raw.scream18, R.raw.scream19, R.raw.scream20, 
				R.raw.scream21, R.raw.scream22, R.raw.scream23}){
			screams.put(counter, soundPool.load(appCtx, id, 1));
			counter++;
		}	
		numGenerator = new Random();
	}
	
	public void scream(){
			soundPool.play(screams.get((int)Math.floor(numGenerator.nextDouble()*screams.size())), 1f, 1f, 1, 0, 1f);
			Log.d(TAG, "Screamed.");
	}
	
	public void release(){
		soundPool.release();
	}
}
