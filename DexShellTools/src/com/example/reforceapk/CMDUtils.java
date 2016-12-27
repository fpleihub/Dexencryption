package com.example.reforceapk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CMDUtils {
	 public static Process CMD(String cmd){
	        Process p = null;
	        try {
	            cmd = "cmd.exe /c "+cmd;
	            System.out.println(cmd);
	            p = Runtime.getRuntime().exec(cmd);
	            new Thread(new cmdResult(p.getInputStream())).start();
	            new Thread(new cmdResult(p.getErrorStream())).start();
	            p.getOutputStream().close();
	        } catch (Exception e) {
	            System.out.println("命令行出错！");
	            e.printStackTrace();
	        }
	        return p;
	    }
	     
	    public static Process CMD(String cmd,String ...args){
	        return CMD(String.format(cmd, args));
	    }
	     
	    public static Process runCMD(String cmd){
	        Process p = null;
	        try {
	            cmd = "cmd.exe /k start "+cmd;
	            System.out.println(cmd);
	            p = Runtime.getRuntime().exec(cmd);
//	            new Thread(new cmdResult(p.getInputStream())).start();
//	            StreamHandler outputStreamHandler = new StreamHandler(p.getErrorStream(), "STDOUT");
//	            outputStreamHandler.start();
//	            new cmdResult(p.getErrorStream());
	            p.getOutputStream().close();
	        } catch (Exception e) {
	            System.out.println("命令行出错！");
	            e.printStackTrace();
	        }
	        return p;
	    }
	     
	    public static Process runCMD(String cmd,String ...args){
	        return runCMD(String.format(cmd, args));
	    }
	     
	    static class cmdResult implements Runnable{
	        private InputStream ins;
	         
	        public cmdResult(InputStream ins){
	            this.ins = ins;
	        }
	 
	        @Override
	        public void run() {
	            try {
	                BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
	                String line = null;
	                while ((line=reader.readLine())!=null) {
	                    System.out.println(line);
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	         
	    }
}
