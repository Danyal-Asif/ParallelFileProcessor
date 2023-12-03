package com.parallelFileProcessor;

import java.io.FileInputStream;
import java.security.MessageDigest;

public class CheckSumCalculator extends Thread {
	
	private String fileName;
	private FileProcessor fileProcessor;
	
	public CheckSumCalculator(String fileName,FileProcessor fileProcessor) {
		this.fileName=fileName;
		this.fileProcessor=fileProcessor;
	}
	 
	@Override
	public void run() {
		try {
			MessageDigest md=MessageDigest.getInstance("MD5");
			FileInputStream fis=new FileInputStream(this.fileName);
			byte[] buffer=new byte[8192];
			int bytesRead;
			while((bytesRead=fis.read(buffer))!=-1) {
				md.update(buffer,0,bytesRead);
			}
			fis.close();
			
			byte[] digest=md.digest();
			StringBuilder result=new StringBuilder();
			for(byte b:digest) {
				result.append(String.format("0%2x", b));
				
			}
			
//			System.out.println("CheckSum for "+this.fileName+" : "+result.toString());
			fileProcessor.addProcessedMD5(result.toString());
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
