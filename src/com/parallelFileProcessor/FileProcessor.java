package com.parallelFileProcessor;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FileProcessor {
    private ExecutorService executor;
    private List<String> fileType;
    private List<String> processedFiles = new ArrayList<>();
    private List<String> processedMD5_code = new ArrayList<>();
    private List<String> processedLinesCount = new ArrayList<>();
    private List<String> filePath= new ArrayList<>();
    Map<String,Boolean> filters;
   
	public FileProcessor(int maxThreads,Map<String,Boolean> ft) {
        this.executor = Executors.newFixedThreadPool(maxThreads);
        fileType=new ArrayList<String>();
        for(String filetype:ft.keySet()) {
        	if(ft.get(filetype)==true) {
        		fileType.add(filetype);
        	}
        }
    }
     
	public void addProcessedMD5(String md5Checksum) {
        processedMD5_code.add(md5Checksum);
    }

    public List<String> getProcessedMD5Codes() {
        return processedMD5_code;
    }

    public List<String> getProcessedFiles() {
        return processedFiles;}
    
    
    
    public List<String> getProcessedLinesCount() {
		return processedLinesCount;
	}

	public void addProcessedLinesCount(String lineCount) {
		processedLinesCount.add(lineCount);
	}
	

	public List<String> getFilePath() {
		return filePath;
	}

	public void addFilePath(String fp) {
		filePath.add(fp);
	}

	public void processFiles(String directoryPath) {
        File directory = new File(directoryPath);

        if (!directory.isDirectory()) {
            System.out.println("Invalid directory path.");
            return;
        }

        processDirectory(directory);
        executor.shutdown();
    }

    private void processDirectory(File directory) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    processDirectory(file);
                } else {
                	String filePath=file.getAbsolutePath();
                	if (fileType.size()==0) {
                		processFile(filePath);
                	}
                	else {
                	for(String ft:fileType) {
                	if(ft!=null && filePath.endsWith(ft)) {
                    processFile(filePath);
                    }
                	}
                	}
                }
            }
        }
    }

    private void processFile(String filePath) {
    	addFilePath(filePath);
    	Path path=Paths.get(filePath);
    	processedFiles.add(path.getFileName().toString());
    	
        CheckSumCalculator checksumCalculator = new CheckSumCalculator(filePath,this);
        LineCounter lineCounter = new LineCounter(filePath,this);

        // Submit tasks
        Future<Void> checksumFuture = executor.submit(() -> {
            checksumCalculator.run();
            return null;
        });
        Future<Void> lineCountFuture = executor.submit(() -> {
            lineCounter.run();
            return null;
        });

       
        try {
            checksumFuture.get();
            lineCountFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        System.out.println("Processing complete for: " + filePath);
    }
}
