package com.handpay.shell;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import dalvik.system.DexClassLoader;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.util.ArrayMap;
import android.util.Log;

/**
 * this class is to auxiliary dex...
 * 
 * @author fplei
 * 
 */
@SuppressLint("NewApi")
public class DexOperation {
	static {
        System.loadLibrary("handpayjiagu");
    }
	/**
	 * 在c++中初始化环境变量
	 * @param pkname
	 * @return
	 */
	static native String[] initEnvriment(String pkname);
	
	static native void nativeEnableTurboDex();

    static native void nativeDisableTurboDex();
    
    public static boolean enableTurboDex() {
        if (isArtMode()) {
        	Log.i("result", "CurrentEvriment is Art,Hook dex");
            try {
                nativeEnableTurboDex();
                return true;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }else{
        }
        return false;
    }
    public static void disableTurboDex() {
        try {
            nativeDisableTurboDex();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
	/**
	 * this function use to Segmentation procedure
	 * 
	 * @throws Exception
	 */
	public static void copyProgressData(Application app, String apkFileName)
			throws Exception {
		File apkFile = new File(apkFileName);
		if (!apkFile.exists()) {
			apkFile.createNewFile();
			byte[] dexdata = readDexFileFromApk(app);
			splitPayLoadFromDex(dexdata, apkFileName);
		}
	}
	/**
	 * this function is that DexLoader to LoadApk
	 */
	public static void hookDexLoader(String packageName, String apkFileName,
			String odexPath, String libPath) {
		Object currentActivityThread = RefInvoke.invokeStaticMethod(
				"android.app.ActivityThread", "currentActivityThread",
				new Class[]{}, new Object[]{});
		WeakReference wr = null;
		if (Build.VERSION.SDK_INT < 19) {
			HashMap mPackages = (HashMap) RefInvoke.getFieldOjbect(
					"android.app.ActivityThread", currentActivityThread,
					"mPackages");
			wr = (WeakReference) mPackages.get(packageName);
		} else {
			ArrayMap mPackages = (ArrayMap) RefInvoke.getFieldOjbect(
					"android.app.ActivityThread", currentActivityThread,
					"mPackages");
			wr = (WeakReference) mPackages.get(packageName);
		}
		DexClassLoader dLoader = new DexClassLoader(apkFileName, odexPath,
				libPath, (ClassLoader) RefInvoke.getFieldOjbect(
						"android.app.LoadedApk", wr.get(), "mClassLoader"));
		RefInvoke.setFieldOjbect("android.app.LoadedApk", "mClassLoader",
				wr.get(), dLoader);
	}

	/**
	 * this function is get byte from dex
	 * 
	 * @return
	 * @throws IOException
	 */
	public static byte[] readDexFileFromApk(Application con) throws IOException {
		ByteArrayOutputStream dexByteArrayOutputStream = new ByteArrayOutputStream();
		ZipInputStream localZipInputStream = new ZipInputStream(
				new BufferedInputStream(new FileInputStream(
						con.getApplicationInfo().sourceDir)));
		while (true) {
			ZipEntry localZipEntry = localZipInputStream.getNextEntry();
			if (localZipEntry == null) {
				localZipInputStream.close();
				break;
			}
			if (localZipEntry.getName().equals("classes.dex")) {
				byte[] arrayOfByte = new byte[1024];
				while (true) {
					int i = localZipInputStream.read(arrayOfByte);
					if (i == -1)
						break;
					dexByteArrayOutputStream.write(arrayOfByte, 0, i);
				}
			}
			localZipInputStream.closeEntry();
		}
		localZipInputStream.close();
		return dexByteArrayOutputStream.toByteArray();
	}

	/**
	 * �ͷű��ӿǵ�apk�ļ���so�ļ�
	 * 
	 * @param data
	 * @throws IOException
	 */
	private static void splitPayLoadFromDex(byte[] apkdata, String apkFileName)
			throws IOException {
		int ablen = apkdata.length;
		byte[] dexlen = new byte[4];
		System.arraycopy(apkdata, ablen - 4, dexlen, 0, 4);
		ByteArrayInputStream bais = new ByteArrayInputStream(dexlen);
		DataInputStream in = new DataInputStream(bais);
		int readInt = in.readInt();
		byte[] newdex = new byte[readInt];
		System.arraycopy(apkdata, ablen - 4 - readInt, newdex, 0, readInt);
		newdex = decrypt(newdex);
		File file = new File(apkFileName);
		try {
			FileOutputStream localFileOutputStream = new FileOutputStream(file);
			localFileOutputStream.write(newdex);
			localFileOutputStream.close();
		} catch (IOException localIOException) {
			throw new RuntimeException(localIOException);
		}
	}
	// //ֱ�ӷ������ݣ����߿�������Լ����ܷ���
	private static byte[] decrypt(byte[] srcdata) {
		for (int i = 0; i < srcdata.length; i++) {
			srcdata[i] = (byte) (0xFF ^ srcdata[i]);
		}
		return srcdata;
	}
	
	public static Application intefaceApplication(Application context,String appClassName){
		if (appClassName != null) {
			Object currentActivityThread = RefInvoke.invokeStaticMethod(
					"android.app.ActivityThread", "currentActivityThread",
					new Class[]{}, new Object[]{});
			Object mBoundApplication = RefInvoke.getFieldOjbect(
					"android.app.ActivityThread", currentActivityThread,
					"mBoundApplication");
			Object loadedApkInfo = RefInvoke.getFieldOjbect(
					"android.app.ActivityThread$AppBindData",
					mBoundApplication, "info");

			RefInvoke.setFieldOjbect("android.app.LoadedApk",
					"mApplication", loadedApkInfo, null);
			Object oldApplication = RefInvoke.getFieldOjbect(
					"android.app.ActivityThread", currentActivityThread,
					"mInitialApplication");

			ArrayList<Application> mAllApplications = (ArrayList<Application>) RefInvoke
					.getFieldOjbect("android.app.ActivityThread",
							currentActivityThread, "mAllApplications");
			mAllApplications.remove(oldApplication);// ɾ��oldApplication

			ApplicationInfo appinfo_In_LoadedApk = (ApplicationInfo) RefInvoke
					.getFieldOjbect("android.app.LoadedApk", loadedApkInfo,
							"mApplicationInfo");

			ApplicationInfo appinfo_In_AppBindData = (ApplicationInfo) RefInvoke
					.getFieldOjbect(
							"android.app.ActivityThread$AppBindData",
							mBoundApplication, "appInfo");

			if (appClassName != null && appClassName.length() > 0) {
				appinfo_In_LoadedApk.className = appClassName;
				appinfo_In_AppBindData.className = appClassName;
			}
			Application app = (Application) RefInvoke.invokeMethod(
					"android.app.LoadedApk", "makeApplication",
					loadedApkInfo, new Class[]{boolean.class,
							Instrumentation.class}, new Object[]{false,
							null});// ִ�� makeApplication��false,null��
			try {
				// ���LoadedApk �����makeApplication
				// û�ɹ�����һ��Instrumentation�����newApplication��������ò�Ҫ�ߵ�������
				if (app == null) {
					Class appli = Class.forName(appClassName);
					app = (Application) RefInvoke.invokeStaticMethod(
							"android.app.Instrumentation",
							"newApplication", new Class[]{Class.class,
									Context.class}, new Object[]{appli,context});
				}
			} catch (Exception e) {
				Log.e("result", e.toString());
			}
			RefInvoke.setFieldOjbect("android.app.ActivityThread",
					"mInitialApplication", currentActivityThread, app);

			if (Build.VERSION.SDK_INT < 19) {// hashmap
				HashMap mProviderMap = (HashMap) RefInvoke.getFieldOjbect(
						"android.app.ActivityThread",
						currentActivityThread, "mProviderMap");
				Iterator it = mProviderMap.values().iterator();
				while (it.hasNext()) {
					Object providerClientRecord = it.next();
					Object localProvider = RefInvoke
							.getFieldOjbect(
									"android.app.ActivityThread$ProviderClientRecord",
									providerClientRecord, "mLocalProvider");
					RefInvoke.setFieldOjbect(
							"android.content.ContentProvider", "mContext",
							localProvider, app);
				}
			} else {
				ArrayMap mProviderMap = (ArrayMap) RefInvoke
						.getFieldOjbect("android.app.ActivityThread",
								currentActivityThread, "mProviderMap");
				Iterator it = mProviderMap.values().iterator();
				while (it.hasNext()) {
					Object providerClientRecord = it.next();
					if (providerClientRecord != null) {
						Object localProvider = RefInvoke
								.getFieldOjbect(
										"android.app.ActivityThread$ProviderClientRecord",
										providerClientRecord,
										"mLocalProvider");
						if (localProvider != null) {
							RefInvoke.setFieldOjbect(
									"android.content.ContentProvider",
									"mContext", localProvider, app);
						}
					}
				}
			}
			return app;
		}
		return null;
	}
	
	/**
     * In current version, only enable TBD at ART mode.
     *
     * @return current Environment is ART mode
     */
    private static boolean isArtMode() {
        return System.getProperty("java.vm.version", "").startsWith("2");
    }
}
