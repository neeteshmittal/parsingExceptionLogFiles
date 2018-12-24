package com.parsing.exceptionfiles.logging;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by Neetesh Mittal on 25/12/18
 */
public class ParsingExceptionFile implements Callable<HashMap<String,Integer>> {

    String filename;
    long lastPointer;
    long startTimeStamp;
    long endTimeStamp;


    public ParsingExceptionFile(String filename, long lastPointer) {
        this.filename = filename;
        this.lastPointer = lastPointer;
    }

    @Override
    public HashMap<String,Integer> call() throws Exception {
        HashMap<String,Integer> exceptionVsCount = new HashMap<>();
        File file = new File(filename);
        RandomAccessFile rf = new RandomAccessFile(file, "r");
        rf.seek(lastPointer);
        String line = null;
        while ((line = rf.readLine()) != null) {
            String[] parsed = line.split(" ");
            String exception = parsed[1];
            long temp = Long.parseLong(parsed[0]);
            if (temp <= endTimeStamp) {
                if (exception != null && !exception.isEmpty()) {
                    int count = 0;
                    if (exceptionVsCount.get(exception) != null) {
                        count = exceptionVsCount.get(exception);
                    }
                    count++;
                    exceptionVsCount.put(exception, count);
                }
            }
            else{
                break;
            }

        }
        lastPointer = rf.getFilePointer();
        ParsingFileHelper.getInstance().fileNameVsLastPointer.put(filename, lastPointer);
        return exceptionVsCount;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setStartTimeStamp(long startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public long getEndTimeStamp() {
        return endTimeStamp;
    }

    public void setEndTimeStamp(long endTimeStamp) {
        this.endTimeStamp = endTimeStamp;
    }

}
