package com.parallelFileProcessor;

import java.io.BufferedReader;
import java.io.FileReader;

public class LineCounter extends Thread {
	
	private String filename;
	private FileProcessor fileProcessor;

    public LineCounter(String filename,FileProcessor fileProcessor) {
        this.filename = filename;
        this.fileProcessor=fileProcessor;
    }
     
    @Override
    public void run() {
    	try {
    	BufferedReader reader=new BufferedReader(new FileReader(this.filename));
    	int lineCount=0;
    	while(reader.readLine() !=null) {
    		lineCount++;
    	}
    	reader.close();
//    	System.out.println("Line Count for "+filename+" : "+lineCount);
    	fileProcessor.addProcessedLinesCount(lineCount+"");
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    			
    }
}
