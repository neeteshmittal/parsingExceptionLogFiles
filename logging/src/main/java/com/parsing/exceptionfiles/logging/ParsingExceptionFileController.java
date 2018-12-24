package com.parsing.exceptionfiles.logging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Created by Neetesh Mittal on 25/12/18
 */
@Controller
public class ParsingExceptionFileController {

    private final ParsingFileHelper parsingFileHelper = ParsingFileHelper.getInstance();


    @GetMapping("/parse")
    public String parseExceptionFile(){
        long startTime = parsingFileHelper.calculateStartTimeStamp();
        long endTime = parsingFileHelper.calculateEndTimeStamp(startTime,15, Calendar.MINUTE);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<HashMap<String,Integer>>> resultList = new ArrayList<>();

        for(String fileName : parsingFileHelper.fileNameVsLastPointer.keySet()) {
            ParsingExceptionFile parsingLogFile = new ParsingExceptionFile(fileName, parsingFileHelper.fileNameVsLastPointer.get(fileName));
            parsingLogFile.setStartTimeStamp(startTime);
            parsingLogFile.setEndTimeStamp(endTime);

            Callable<HashMap<String, Integer>> callable = parsingLogFile;
            Future<HashMap<String, Integer>> future = executorService.submit(callable);
            resultList.add(future);
        }

        HashMap<String,Integer> finalOutputMap = parsingFileHelper.getFinalHashMap(resultList);
        try {
            parsingFileHelper.modifyingOutputFile(finalOutputMap, "output.txt", startTime, endTime);
        } catch (IOException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        return "redirect:/";
    }

    /*@PostMapping("/addFile/{filename}/{filePointer}")
    public String addFileInMap(@PathVariable String filename, @PathVariable String filePointer){
        parsingFileHelper.addingEntryInMap(filename, Long.parseLong(filePointer));
        return "redirect:/";
    }

    @PostMapping("/removeFile/{filename}")
    public String addFileInMap(@PathVariable String filename){
        parsingFileHelper.removeEntryInMap(filename);
        return "redirect:/";
    }*/
}
