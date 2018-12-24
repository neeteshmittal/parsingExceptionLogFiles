package com.parsing.exceptionfiles.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Neetesh Mittal on 25/12/18
 */
public class ParsingFileHelper {

    private static ParsingFileHelper instance;

    ConcurrentHashMap<String, Long> fileNameVsLastPointer;

    private ParsingFileHelper(){}

    public static ParsingFileHelper getInstance(){
        if(instance == null){
            synchronized (ParsingFileHelper.class){
                if(instance ==null) {
                    instance = new ParsingFileHelper();
                }
            }
        }
        return instance;
    }

    public void initMap(){
        fileNameVsLastPointer = new ConcurrentHashMap<>();
        fileNameVsLastPointer.put("errorlog.txt", 0L);
        fileNameVsLastPointer.put("errorlog1.txt", 0L);
    }

    public void addingEntryInMap(String filename, long filePointer){
        fileNameVsLastPointer.put(filename,filePointer);
    }

    public void removeEntryInMap(String filename){
        fileNameVsLastPointer.remove(filename);
    }

    public HashMap<String,Integer> getFinalHashMap(List<Future<HashMap<String,Integer>>> resultList){
        HashMap<String,Integer> finalOutputMap = new HashMap<>();
        for(Future<HashMap<String,Integer>> tempFuture: resultList){
            try {
                HashMap<String, Integer> tempMap = tempFuture.get();
                for(String exception: tempMap.keySet()){
                    int count =tempMap.get(exception);
                    if(finalOutputMap.get(exception)!=null){
                        count += finalOutputMap.get(exception);
                    }
                    finalOutputMap.put(exception,count);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }
        return finalOutputMap;
    }

    public  void modifyingOutputFile(HashMap<String,Integer> outputMap, String fileName, long startTime, long endTime)
        throws IOException {
        File outfile = new File(fileName);
        try( FileWriter rf = new FileWriter(outfile, true)) {
            for(String exception: outputMap.keySet()){
                StringBuilder sb = new StringBuilder();
                sb.append(startTime +" - " + endTime+ " : ");
                sb.append(exception);
                sb.append(" ");
                sb.append(outputMap.get(exception));
                sb.append("\n");
                rf.write(sb.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  long calculateStartTimeStamp(){

        long startTime = Long.MAX_VALUE ;

        for(String fileName : fileNameVsLastPointer.keySet()){
            File file = new File(fileName);
            try(RandomAccessFile rf = new RandomAccessFile(file, "r")) {
                rf.seek(fileNameVsLastPointer.get(fileName));
                String line = null;
                if ((line = rf.readLine()) != null) {
                    String[] parsed = line.split(" ");
                    long temp = Long.parseLong(parsed[0]);
                    if (startTime > temp) {
                        startTime = temp;
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return startTime;
    }

    public  long calculateEndTimeStamp(long startTimeStamp, int interval, int base){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(startTimeStamp));
        cal.add(base, interval);
        return cal.getTime().getTime();
    }

}
