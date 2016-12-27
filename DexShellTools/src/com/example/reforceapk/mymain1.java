package com.example.reforceapk;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


public class mymain1 {
	private static String rename_application="com.example.reforceapk.ProxyApplication";
	/*private static String key_store_name="kalefu";
	private static String key_store_path="D:\\workspace\\NFCOEM\\"+key_store_name;
	private static String key_store_pwd="client";
	private static String key_aleas="client-test";*/
	private static String key_store_name="client-test.jks";
	private static String key_store_path="D:\\workspace\\trunk\\"+key_store_name;
	private static String key_store_pwd="client";
	private static String key_aleas="client-test";
	public enum OperationModle{
		ENCRY_DEX,
		ENCRY_APK,
		ENCRY_JAR
	}
	private static OperationModle operation=OperationModle.ENCRY_APK;
	private static String apkoutputdir="E:\\output\\autoEncryapk";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String apk_file_path="force";
		getNeedEncrFile(apk_file_path);
	}
	
	private static String parent_root=null;
	//读取释放apk文件
	public static void getNeedEncrFile(String apk_file_path){
		File loadNeedEncFile = new File(apk_file_path);
		parent_root=loadNeedEncFile.getAbsolutePath();
		System.out.println("操作根目录:"+parent_root);
		//解包模板
		unpackge(new File(apk_file_path+"\\ModleApk.apk"));
		
		if(loadNeedEncFile.isDirectory()){
			File[] objfiles=loadNeedEncFile.listFiles();
			System.out.println("force文件夹下有"+objfiles.length+"个文件！");
			for(File file:objfiles){
				System.out.println("文件名："+file.getName()+",大小："+file.length()+",路径："+file.getAbsolutePath());
				if(file.getName().contains(".apk")&&!file.getName().equals("ModleApk.apk")){
					DecoderApk(file);
				}
			}
			/*File loadNeedEncFile1 = new File(parent_root);
			//删除未签名的文件
			for(File file1:loadNeedEncFile1.listFiles()){
				if(file1.getName().contains("unsing.apk")){
					FileUtils.deleteFile("force\\"+file1.getName());
				}
			}*/
		}
	}
	private static void unpackge(File file){
		try{
			
			File outputfile=new File(parent_root+"\\"+file.getName().substring(0, file.getName().lastIndexOf(".")));
			System.out.println(outputfile.getAbsolutePath());
			// d 表示编译/b表示重新打包    -s 表示不编译dex文件  
	//		String cmdUnpack = "tools\\apktool.jar d "+ file.getAbsolutePath()+" -o "+outputfile.getAbsolutePath();
			String cmdUnpack = "tools\\apktool.jar d -s "+ file.getAbsolutePath()+" -o "+outputfile.getAbsolutePath();
			System.out.println("正在执行解包命令....");
			CMDUtils.runCMD(cmdUnpack);
			Thread.sleep(4000);
			CMDUtils.CMD("exit");//记得退出，否则文件找不到
		}catch(Exception e){
			
		}
	}
	//反编译 
	public static void DecoderApk(final File file){
		try{
			long starttime=System.currentTimeMillis();
			unpackge(file);
			long cmd_curr_time=System.currentTimeMillis();
			System.out.println(file.getName()+"------------->解包完成！");
			System.out.println(file.getName()+"------------->解包指令执行时间："+(cmd_curr_time-starttime)+"ms");
			//dex打成jar---文件被输出到项目根目录下  这种方式可以
			/*String cmd_dex2jar="tools\\dex2jar\\dex2jar.bat force\\"+file.getName().substring(0, file.getName().lastIndexOf("."))+"\\classes.dex";
			System.out.println("dex---->jar："+cmd_dex2jar);
			CMDUtils.runCMD(cmd_dex2jar);
			Thread.sleep(2000);
			CMDUtils.CMD("exit");*/
			//修改项目信息
			updateApkPkg(file);
			long cmd_curr_updatexml=System.currentTimeMillis();
			System.out.println("------------->AndroidManifest.xml修改完毕，耗时:"+(cmd_curr_updatexml-cmd_curr_time)+"ms");
			//加壳
			String path="force\\"+file.getName();
			encry_dex(new File(path));
			long curr_encry_time=System.currentTimeMillis();
			System.out.println("------------->加壳使用时间："+(curr_encry_time-cmd_curr_updatexml)+"ms");
			
			//重新打包
			System.out.println("------------->开始重新打包");
			String unsignApk = "ModleApk_jiagu_unsing.apk";
			String cmdpack="tools\\apktool.jar b force\\ModleApk -o force\\"+unsignApk;
			System.out.println("----->cmdpack:"+cmdpack);
			CMDUtils.runCMD(cmdpack);
			Thread.sleep(6000);
			long curr_pack_time=System.currentTimeMillis();
			System.out.println("------------->重新打包耗时："+(curr_pack_time-curr_encry_time)+"ms");
			//签名
			System.out.println("------------->开始重新签名");
			String outputsingapkname=file.getName().substring(0, file.getName().lastIndexOf("."))+"_jiagu_sing.apk";
			String cmdsingapk="jarsigner -digestalg SHA1 -sigalg MD5withRSA -verbose -keystore "+key_store_path+" -storepass "+key_store_pwd+" -signedjar "+apkoutputdir+"\\"+outputsingapkname+" force\\"+unsignApk+" "+key_aleas;
			CMDUtils.runCMD(cmdsingapk);
			Thread.sleep(6000);
			CMDUtils.CMD("exit");
			long curr_singapk_time=System.currentTimeMillis();
			System.out.println("------------->签名耗时："+(curr_singapk_time-curr_pack_time)+"ms");
			//删除该apk  防止重复加壳
//			finshed(file);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//修改包信息
	public static void updateApkPkg(File file){
		//顺便把lib也删除
		String libpathdirs="force\\"+file.getName().substring(0, file.getName().lastIndexOf("."))+"\\lib";
		System.out.println("lib dirs:"+libpathdirs);
		File libsdirfile=new File(libpathdirs);
		if(libsdirfile.exists()){
			if(libsdirfile.isDirectory()){
				File[] files=libsdirfile.listFiles();
				for(File fileobj:files){
					File temp_f=fileobj;
					if(temp_f.isDirectory()){
						File[] temps=temp_f.listFiles();
						for(File tempfile:temps){
							tempfile.delete();
							System.out.println("-------->"+tempfile.getName()+"被删除！");
						}
					}else{
						fileobj.delete();
					}
				}
			}else{
				libsdirfile.delete();
			}
		}
		try{
		//修改AndroidManifest文件信息
		String mainfest_path="force\\"+file.getName().substring(0, file.getName().lastIndexOf("."))+"\\AndroidManifest.xml";
		System.out.println(mainfest_path);
		File manifest_file=new File(mainfest_path);
		if(!manifest_file.exists()){
			System.out.println("解包后AndroidManifest.xml文件不存在！");
			return;
		}
		Document doc=XMLPares1.parse("force\\"+file.getName().substring(0, file.getName().lastIndexOf("."))+"\\AndroidManifest.xml");
		Element elt_root=doc.getDocumentElement();
		Element elt_application=XMLPares1.getChildElement(elt_root, "application");
		//String add_child_elt="<meta-data android:name=\"APPLICATION_CLASS_NAME\" android:value=\""+elt_application.getAttribute("android:name")+"\"/>";
		Element child_elt_tag=doc.createElement("meta-data");
			child_elt_tag.setAttribute("android:name", "APPLICATION_CLASS_NAME");
			String tag_application_value=elt_application.getAttribute("android:name");
			if(tag_application_value!=null&&tag_application_value.length()>0){
				child_elt_tag.setAttribute("android:value", tag_application_value);
			}else{
				child_elt_tag.setAttribute("android:value", "");
			}
			elt_application.appendChild(child_elt_tag);
		elt_application.setAttribute("android:name",rename_application);
		String update_xml =XMLPares1.doc2String(doc);
		mainfest_path="force\\ModleApk\\AndroidManifest.xml";
		writeString(mainfest_path,update_xml);
		}catch(Exception e){
			e.printStackTrace();
			return ;
		}
	}
	
	public static void finshed(File file){
		//删除未签名apk
		File temp_file=file;
//		FileUtils.deleteFile("force\\"+temp_file.getName());
		FileUtils.deleteFolder("force\\"+temp_file.getName().substring(0, temp_file.getName().lastIndexOf(".")));
	}
	
	
	//<?xml version="1.0" encoding="utf-8"?>
	public static String read(String path) {  
        StringBuffer res = new StringBuffer();  
        String line = null;  
        try {  
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));  
            while ((line = reader.readLine()) != null) {  
            		res.append(line + "\n"); 
            }  
            reader.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return res.toString();  
    }
	
	public static void writeString(String outpath,String context){
		try{
			File file=new File(outpath);
			if(file.exists()){
				file.delete();
			}
			String temp_context=context;
			FileOutputStream fileout=new FileOutputStream(outpath);
			byte[] temp_type=temp_context.getBytes(Charset.forName("UTF-8"));
			fileout.write(temp_type);
			fileout.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void encry_dex(File targetdex){
		try{
			System.out.println("----------------------》开始加壳...."+targetdex.getPath());
			String temp_dex2jar_name="classes_dex2jar.jar";
			
			File targetdexfile=null;
			if(operation==OperationModle.ENCRY_APK){
				targetdexfile = targetdex;   //需要加壳的程序apk
			}else if(operation==OperationModle.ENCRY_DEX){
				targetdexfile=new File("force\\"+targetdex.getName().substring(0, targetdex.getName().lastIndexOf("."))+"\\classes.dex");
			}else if(operation==OperationModle.ENCRY_JAR){
				targetdexfile=new File("force\\"+targetdex.getName().substring(0, targetdex.getName().lastIndexOf("."))+"\\"+temp_dex2jar_name);
			}
			File otherdexfile=new File("force\\otherClasses.dex");//壳dex
			byte[] targetdexArray = encrpt(readFileBytes(targetdexfile));//以二进制形式读出apk，并进行加密处理
			byte[] otherdexArray = readFileBytes(otherdexfile);//以二进制形式读出壳dex
			int targetdexLen = targetdexArray.length;
			System.out.println("---------》需要加密的dex长度"+targetdexLen);
			int otherdexLen = otherdexArray.length;
			System.out.println("---------》壳dex长度"+otherdexLen);
			int totalLen = targetdexLen + otherdexLen +4;//多出4字节是存放长度的。
			byte[] newdex = new byte[totalLen]; // 申请了新的长度
			System.arraycopy(otherdexArray, 0, newdex, 0, otherdexLen);//先拷贝dex内容
			System.arraycopy(targetdexArray, 0, newdex, otherdexLen, targetdexLen);//添加源dex
			System.arraycopy(intToByte(targetdexLen), 0, newdex, totalLen-4, 4);//最后4为长度
			//修改合并之后DEX file size文件头
			fixFileSizeHeader(newdex);
			//修改合并之后DEX SHA1 文件头
			fixSHA1Header(newdex);
			//修改合并之后DEX CheckSum文件头
			fixCheckSumHeader(newdex);
			String temp_outputpath="force\\"+targetdex.getName().substring(0, targetdex.getName().lastIndexOf("."))+"\\classes.dex";
			temp_outputpath="force\\ModleApk\\classes.dex";
			System.out.println("----------------------》新dex输出路径："+temp_outputpath);
			File file = new File(temp_outputpath);
			if (!file.exists()) {
				file.createNewFile();
			}else{
				file.deleteOnExit();
				file.createNewFile();
			}
			FileOutputStream localFileOutputStream = new FileOutputStream(temp_outputpath);
			localFileOutputStream.write(newdex);
			localFileOutputStream.flush();
			localFileOutputStream.close();
			File filetest = new File(temp_outputpath);
			if(filetest.exists()){
				System.out.println("-----------------------》合并加壳后长度："+filetest.length());
				System.out.println("------------》加壳完成！");
			}
		}catch(Exception e){
			System.out.println("------------》加壳异常！");
			e.printStackTrace();
			return;
		}
	}
	
	
	//加壳入口
	public static void pre_main(){
		try {
			File payloadSrcFile = new File("force/NFCPOS_ResLib-release.apk");   //需要加壳的程序
			System.out.println("需要加壳apk size:"+payloadSrcFile.length());
			File unShellDexFile = new File("force/otherClasses.dex");	//解壳dex
			System.out.println("脱壳dex size:"+unShellDexFile.length());
			byte[] payloadArray = encrpt(readFileBytes(payloadSrcFile));//以二进制形式读出apk，并进行加密处理//对源Apk进行加密操作
			byte[] unShellDexArray = readFileBytes(unShellDexFile);//以二进制形式读出dex
			int payloadLen = payloadArray.length;
			int unShellDexLen = unShellDexArray.length;
			int totalLen = payloadLen + unShellDexLen +4;//多出4字节是存放长度的。
			byte[] newdex = new byte[totalLen]; // 申请了新的长度
			//添加解壳代码    src  startpos  targetobject targetstartpos length
			System.arraycopy(unShellDexArray, 0, newdex, 0, unShellDexLen);//先拷贝dex内容
			//添加加密后的apk数据
			System.arraycopy(payloadArray, 0, newdex, unShellDexLen, payloadLen);//再在dex内容后面拷贝apk的内容
			//添加加密apk数据长度
			System.arraycopy(intToByte(payloadLen), 0, newdex, totalLen-4, 4);//最后4为长度
            //修改合并之后DEX file size文件头
			fixFileSizeHeader(newdex);
			//修改合并之后DEX SHA1 文件头
			fixSHA1Header(newdex);
			//修改合并之后DEX CheckSum文件头
			fixCheckSumHeader(newdex);
			String str = "force/classes.dex";
			File file = new File(str);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream localFileOutputStream = new FileOutputStream(str);
			localFileOutputStream.write(newdex);
			localFileOutputStream.flush();
			localFileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//直接返回数据，读者可以添加自己加密方法
	private static byte[] encrpt(byte[] srcdata){
		for(int i = 0;i<srcdata.length;i++){
			srcdata[i] = (byte)(0xFF ^ srcdata[i]);
		}
		return srcdata;
	}

	/**
	 * 修改dex头，CheckSum 校验码
	 * @param dexBytes
	 */
	private static void fixCheckSumHeader(byte[] dexBytes) {
		Adler32 adler = new Adler32();
		adler.update(dexBytes, 12, dexBytes.length - 12);//从12到文件末尾计算校验码
		long value = adler.getValue();
		int va = (int) value;
		byte[] newcs = intToByte(va);
		//高位在前，低位在前掉个个
		byte[] recs = new byte[4];
		for (int i = 0; i < 4; i++) {
			recs[i] = newcs[newcs.length - 1 - i];
			System.out.println(Integer.toHexString(newcs[i]));
		}
		System.arraycopy(recs, 0, dexBytes, 8, 4);//效验码赋值（8-11）
		System.out.println("CheckSum:"+Long.toHexString(value));
		System.out.println();
	}


	/**
	 * int 转byte[]
	 * @param number
	 * @return
	 */
	public static byte[] intToByte(int number) {
		byte[] b = new byte[4];
		for (int i = 3; i >= 0; i--) {
			b[i] = (byte) (number % 256);
			number >>= 8;
		}
		return b;
	}

	/**
	 * 修改dex头 sha1值
	 * @param dexBytes
	 * @throws NoSuchAlgorithmException
	 */
	private static void fixSHA1Header(byte[] dexBytes)
			throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(dexBytes, 32, dexBytes.length - 32);//从32为到结束计算sha--1
		byte[] newdt = md.digest();
		System.arraycopy(newdt, 0, dexBytes, 12, 20);//修改sha-1值（12-31）
		//输出sha-1值，可有可无
		String hexstr = "";
		for (int i = 0; i < newdt.length; i++) {
			hexstr += Integer.toString((newdt[i] & 0xff) + 0x100, 16)
					.substring(1);
		}
		System.out.println("SHA1:"+hexstr);
	}

	/**
	 * 修改dex头 file_size值
	 * @param dexBytes
	 */
	private static void fixFileSizeHeader(byte[] dexBytes) {
		//新文件长度
		byte[] newfs = intToByte(dexBytes.length);
		System.out.println(Integer.toHexString(dexBytes.length));
		byte[] refs = new byte[4];
		//高位在前，低位在前掉个个
		for (int i = 0; i < 4; i++) {
			refs[i] = newfs[newfs.length - 1 - i];
			System.out.println(Integer.toHexString(newfs[i]));
		}
		System.arraycopy(refs, 0, dexBytes, 32, 4);//修改（32-35）
	}


	/**
	 * 以二进制读出文件内容
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static byte[] readFileBytes(File file) throws IOException {
		byte[] arrayOfByte = new byte[1024];
		ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
		FileInputStream fis = new FileInputStream(file);
		while (true) {
			int i = fis.read(arrayOfByte);
			if (i != -1) {
				localByteArrayOutputStream.write(arrayOfByte, 0, i);
			} else {
				return localByteArrayOutputStream.toByteArray();
			}
		}
	}
}
