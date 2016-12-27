package com.example.reforceapk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamHandler extends Thread {
	InputStream m_inputStream;
	String m_type;

	public StreamHandler(InputStream is, String type)
	    {
	        this.m_inputStream = is;
	        this.m_type = type;
	    }

	@Override
	    public void run()

	{
	        InputStreamReader isr = null;
	        BufferedReader br = null;

	        try
	        {

	            //设置编码方式，否则输出中文时容易乱码
	            isr = new InputStreamReader(m_inputStream, "GBK");
	            br = new BufferedReader(isr);
	            String line=null;
	            while ( (line = br.readLine()) != null)
	            {
	                System.out.println("PRINT > " + m_type + " : " + line);
	            }
	        }
	        catch (IOException ioe)
	        {
	            ioe.printStackTrace();
	        }
	        finally
	        {
	            try
	            {
	                br.close();
	                isr.close();
	            }
	            catch (IOException ex)
	            {
	               System.out.println(ex.toString());
	            }
	        }
	    }
}
