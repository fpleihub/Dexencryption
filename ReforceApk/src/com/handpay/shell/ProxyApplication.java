package com.handpay.shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
/**
 * 
 * @author fplei
 *
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class ProxyApplication extends Application {
	private static final String appkey = "APPLICATION_CLASS_NAME";
	private String apkFileName;
	private String odexPath;
	private String libPath;
	private boolean isSupportARM7 = false;
	private boolean isSupportARM = true;
	private String packageName = null;
	
	/***
	 * this function use to init runing Environment
	 */
	private void initApplicationEnvriment() {
		String packageName=this.getPackageName();
		this.packageName=packageName;
		String[] paths=DexOperation.initEnvriment(packageName);
		File libdir = new File(paths[0]);
		odexPath = paths[1];
		Log.i("result", "odexPath:"+odexPath);
		if (PlatformInfo.getAPIVersion() < 18) {
			if (libdir.listFiles().length <= 0) {
				copyLib(libdir);
			}
			libPath = paths[0];
		} else {
			libPath = PlatformInfo.getAppLibDir(packageName);
		}
		Log.i("result", "libPath:"+libPath);
		String name = packageName.substring(packageName.lastIndexOf(".") + 1);
		apkFileName = paths[2] + "/" + name + ".dex";
		Log.i("result", "apkFileName:"+apkFileName);
	}

	@Override
	protected void attachBaseContext(Context base) {
		boolean flag=DexOperation.enableTurboDex();
		Log.i("result", "load dex enableTurboDex:"+flag);
		super.attachBaseContext(base);
		try {
			Log.i("result", "Build.VERSION.SDK_INT:" + Build.VERSION.SDK_INT);
			long curr_start_init=System.currentTimeMillis();
			initApplicationEnvriment();
			long curr_end_init=System.currentTimeMillis();
			Log.i("result", "init_envriment_time:"+(curr_end_init-curr_start_init));
			DexOperation.copyProgressData(this,apkFileName);
			long curr_end_copyprogress=System.currentTimeMillis();
			Log.i("result", "copyprogressData_time:"+(curr_end_copyprogress-curr_end_init));
			DexOperation.hookDexLoader(packageName,apkFileName,odexPath,libPath);
			long curr_end_chookdex=System.currentTimeMillis();
			Log.i("result", "chookdex_time:"+(curr_end_chookdex-curr_end_copyprogress));
		} catch (Exception e) {
			Log.i("result", "error:" + Log.getStackTraceString(e));
			e.printStackTrace();
		}
	}
	
	@Override
	public void onCreate() {
			long curr_start_intefaceApplication=System.currentTimeMillis();
			String appClassName = null;
			try {
				ApplicationInfo ai = this.getPackageManager()
						.getApplicationInfo(this.getPackageName(),
								PackageManager.GET_META_DATA);
				Bundle bundle = ai.metaData;
				if (bundle != null
						&& bundle.containsKey("APPLICATION_CLASS_NAME")) {
					appClassName = bundle.getString("APPLICATION_CLASS_NAME");// className
				} else {
					Log.i("result", "have no application class name");
					return;
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			Application newapp=DexOperation.intefaceApplication(this, appClassName);
			long curr_end_intefaceApplication=System.currentTimeMillis();
			Log.i("result", "intefaceapplication_time"+(curr_end_intefaceApplication-curr_start_intefaceApplication));
			if(newapp!=null){
				newapp.onCreate();
			}
			DexOperation.disableTurboDex();
	}

	

	private void copyLib(File newlib) {
		File old = new File(PlatformInfo.getAppLibDir(this.getPackageName()));
		if (!old.exists()) {
			Log.i("result", "lib not find");
		}
		for (File file : old.listFiles()) {
			String libname = file.getName();
			File storeFile = new File(newlib.getAbsolutePath() + "/" + libname);
			try {
				storeFile.createNewFile();
				nioTransferCopy(file, storeFile);
			} catch (Exception e) {
			}
		}
		for (File file : old.listFiles()) {
			file.delete();
		}
	}

	private static void nioTransferCopy(File source, File target) {
		FileChannel in = null;
		FileChannel out = null;
		FileInputStream inStream = null;
		FileOutputStream outStream = null;
		try {
			inStream = new FileInputStream(source);
			outStream = new FileOutputStream(target);
			in = inStream.getChannel();
			out = outStream.getChannel();
			in.transferTo(0, in.size(), out);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inStream.close();
				in.close();
				outStream.close();
				out.close();
			} catch (Exception ex) {

			}
		}
	}

	protected AssetManager mAssetManager;
	protected Resources mResources;
	protected Theme mTheme;

	protected void loadResources(String dexPath) {
		try {
			AssetManager assetManager = AssetManager.class.newInstance();
			Method addAssetPath = assetManager.getClass().getMethod(
					"addAssetPath", String.class);
			addAssetPath.invoke(assetManager, dexPath);
			mAssetManager = assetManager;
		} catch (Exception e) {
			Log.i("inject", "loadResource error:" + Log.getStackTraceString(e));
			e.printStackTrace();
		}
		Resources superRes = super.getResources();
		superRes.getDisplayMetrics();
		superRes.getConfiguration();
		mResources = new Resources(mAssetManager, superRes.getDisplayMetrics(),
				superRes.getConfiguration());
		mTheme = mResources.newTheme();
		mTheme.setTo(super.getTheme());
	}

	@Override
	public AssetManager getAssets() {
		return mAssetManager == null ? super.getAssets() : mAssetManager;
	}

	@Override
	public Resources getResources() {
		return mResources == null ? super.getResources() : mResources;
	}

	@Override
	public Theme getTheme() {
		return mTheme == null ? super.getTheme() : mTheme;
	}

	private void isSupport(Object[] arch) {
		if ("ARM".equals(arch[0])) {
			try {
				boolean isV7NeonCpu = "neon".equals(arch[2]);
				boolean isV7 = ((Integer) arch[1]) == 7 && "".equals(arch[2]);
				boolean isV6 = ((Integer) arch[1]) == 6;
				if (isV7NeonCpu) {
					isSupportARM7 = true;
					isSupportARM = false;
				} else if (isV7) {
					isSupportARM7 = true;
					isSupportARM = false;
				} else if (isV6) {
					isSupportARM7 = false;
					isSupportARM = true;
				} else {
					isSupportARM7 = false;
					isSupportARM = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("INTEL".equals(arch[0])) {
			isSupportARM7 = false;
			isSupportARM = true;
		}
	}
	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}
}
