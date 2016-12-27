package com.handpay.shell;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Build;

public class PlatformInfo {
	
	public static Object[] getCpuArchitecture() {
		Object[] mArmArchitecture=new Object[3];
		mArmArchitecture[0]=-1;
		mArmArchitecture[1]=-1;
		mArmArchitecture[2]=-1;
	    if ((Integer) mArmArchitecture[1] != -1) {  
	        return mArmArchitecture;  
	    }  
	    try {  
	        InputStream is = new FileInputStream("/proc/cpuinfo");  
	        InputStreamReader ir = new InputStreamReader(is);  
	        BufferedReader br = new BufferedReader(ir);  
	        try {  
	            String nameProcessor = "Processor";  
	            String nameFeatures = "Features";  
	            String nameModel = "model name";  
	            String nameCpuFamily = "cpu family";  
	            while (true) {  
	                String line = br.readLine();  
	                String[] pair = null;  
	                if (line == null) {  
	                    break;  
	                }  
	                pair = line.split(":");  
	                if (pair.length != 2)  
	                    continue;  
	                String key = pair[0].trim();  
	                String val = pair[1].trim();  
	                if (key.compareTo(nameProcessor) == 0) {  
	                    String n = "";  
	                    for (int i = val.indexOf("ARMv") + 4; i < val.length(); i++) {  
	                        String temp = val.charAt(i) + "";  
	                        if (temp.matches("\\d")) {  
	                            n += temp;  
	                        } else {  
	                            break;  
	                        }  
	                    }  
	                    mArmArchitecture[0] = "ARM";  
	                    mArmArchitecture[1] = Integer.parseInt(n);  
	                    continue;  
	                }  
	  
	                if (key.compareToIgnoreCase(nameFeatures) == 0) {  
	                    if (val.contains("neon")) {  
	                        mArmArchitecture[2] = "neon";  
	                    }  
	                    continue;  
	                }  
	  
	                if (key.compareToIgnoreCase(nameModel) == 0) {  
	                    if (val.contains("Intel")) {  
	                        mArmArchitecture[0] = "INTEL";  
	                        mArmArchitecture[2] = "atom";  
	                    }  
	                    continue;  
	                }  
	  
	                if (key.compareToIgnoreCase(nameCpuFamily) == 0) {  
	                    mArmArchitecture[1] = Integer.parseInt(val);  
	                    continue;  
	                }  
	            }  
	        } finally {  
	            br.close();  
	            ir.close();  
	            is.close();  
	        }  
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	    return mArmArchitecture;  
	}  
	
	public static int getAPIVersion(){
		return Build.VERSION.SDK_INT;
	}
	
	public static String getAppLibDir(String pkg){
		return "/data/data/"+pkg+"/lib";
	}
}
