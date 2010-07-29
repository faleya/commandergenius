// This string is autogenerated by ChangeAppSettings.sh, do not change spaces amount
package net.sourceforge.jooleem;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.os.PowerManager;
import android.widget.TextView;


public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// fullscreen mode
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				   WindowManager.LayoutParams.FLAG_FULLSCREEN); 

		_tv = new TextView(this);
		_tv.setText("Initializing");
		setContentView(_tv);

		Settings.Load(this);

		mLoadLibraryStub = new LoadLibrary();
		mAudioThread = new AudioThread(this);
	}
	
	public void startDownloader()
	{
		if( downloader == null )
			downloader = new DataDownloader(this, _tv);
	}

	public void initSDL()
	{
		if(sdlInited)
			return;
		sdlInited = true;
		mGLView = new DemoGLSurfaceView(this);
		setContentView(mGLView);
		// Receive keyboard events
		mGLView.setFocusableInTouchMode(true);
		mGLView.setFocusable(true);
		mGLView.requestFocus();
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, Globals.ApplicationName);
		wakeLock.acquire();
	}

	@Override
	protected void onPause() {
		if( downloader != null ) {
			synchronized( downloader ) {
					downloader.setParent(null, null);
			}
		}
		// TODO: if application pauses it's screen is messed up
		if( wakeLock != null )
			wakeLock.release();
		super.onPause();
		if( mGLView != null )
			mGLView.onPause();
	}

	@Override
	protected void onResume() {
		if( wakeLock != null )
			wakeLock.acquire();
		super.onResume();
		if( mGLView != null )
			mGLView.onResume();
		if( downloader != null ) {
			synchronized( downloader ) {
				downloader.setParent(this, _tv);
				if( downloader.DownloadComplete )
					initSDL();
			}
		}
	}

	@Override
	protected void onStop() 
	{
		if( downloader != null ) {
			synchronized( downloader ) {
					downloader.setParent(null, null);
			}
		}
		if( wakeLock != null )
			wakeLock.release();
		
		if( mGLView != null )
			mGLView.exitApp();
		super.onStop();
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, final KeyEvent event) {
		// Overrides Back key to use in our app
		 if( mGLView != null )
			 mGLView.nativeKey( keyCode, 1 );
		 if( keyCode == KeyEvent.KEYCODE_BACK && !downloader.DownloadComplete )
			 onStop();
		 return true;
	}
	
	@Override
	public boolean onKeyUp(int keyCode, final KeyEvent event) {
		 if( mGLView != null )
			 mGLView.nativeKey( keyCode, 0 );
		 return true;
	}
	
	@Override
	public boolean dispatchTouchEvent(final MotionEvent ev) {
		if(mGLView != null)
			mGLView.onTouchEvent(ev);
		return true;
	}

	private DemoGLSurfaceView mGLView = null;
	private LoadLibrary mLoadLibraryStub = null;
	private AudioThread mAudioThread = null;
	private PowerManager.WakeLock wakeLock = null;
	private static DataDownloader downloader = null;
	private TextView _tv = null;
	private boolean sdlInited = false;

}
