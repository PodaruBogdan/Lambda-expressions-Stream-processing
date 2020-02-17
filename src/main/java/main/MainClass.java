package main;

import activityProcess.MonitoredData;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainClass {
    public static void main(String[] args) throws IOException {
        FileWriter fw=null;
        //create monitored data list from file
        List<MonitoredData> monitoredData=null;
          try {
            Stream<String> lines=Files.lines(Paths.get("Activities.txt"));
            monitoredData=lines
                    .map(line-> new MonitoredData(line.split("\\s\\s+")))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fw=new FileWriter("monitoredData.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
          for(int i=0;i<monitoredData.size();i++)
          {
              fw.append(monitoredData.get(i)+"\n");
          }
          fw.close();
       //count how many days were monitored
        Stream stream=Stream.concat(monitoredData.stream().map(start->start.getStart_time().getDate()),
                monitoredData.stream().map(end->end.getEnd_time().getDate()));
          long nrOfDays=stream.distinct().count();
        try {
            fw=new FileWriter("totalMonitoredDays.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        fw.append("Total number of monitored days: "+nrOfDays);
        fw.close();
        //how many times each activity appeared
        Map<String, Long> occurrences = monitoredData.stream()
                .collect(Collectors.groupingBy(a -> a.getActivity(),
                        Collectors.counting()));
        try {
            fw=new FileWriter("eachActAppeared.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(Map.Entry<String,Long> entry:occurrences.entrySet())
        {
            fw.append(entry.getKey()+" -> "+entry.getValue()+"\n");
        }
        fw.close();
        //count activity occurrence/day
        //count start days
        Map<String,Map<String,Long>> s1=monitoredData.stream()
                .collect(Collectors.groupingBy(a->a.getStartIdentifier(),
                        Collectors.groupingBy(a->a.getActivity(),Collectors.counting())));
        //count end days
        Map<String,Map<String,Long>> s2=monitoredData.stream()
                .collect(Collectors.groupingBy(a->a.getEndIdentifier(),
                        Collectors.groupingBy(a->a.getActivity(),Collectors.counting())));
        //merge start and end days
        s2.forEach((k,v)->s1.merge(k,v,
                (m1,m2)->{
                    Map<String,Long> m=new HashMap<>(m1);
                    for(Map.Entry<String,Long> entry:m2.entrySet()) {
                        String key=entry.getKey();
                        if(m.containsKey(key) && m2.get(key)>m.get(key))
                            m.put(key,m2.get(key));
                        if(!m.containsKey(key))
                        {
                            m.put(key,m2.get(key));
                        }
                    }
                    return m;
                }
                ));
        //display
        try {
             fw=new FileWriter("occPerDay.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(Map.Entry entry:s1.entrySet())
        {
            fw.append("   Day : "+entry.getKey()+"\n");
            Map<String,Long> insideEntry=(Map<String, Long>)entry.getValue();
            for(Map.Entry it:insideEntry.entrySet()){
                fw.append("Activity : "+it.getKey()+" occurrence/day : "+it.getValue()+"\n");
            }
        }
        fw.close();
        //duration for each monitored data activity
        Map<MonitoredData, Long> durations = monitoredData.stream()
                .collect(Collectors.toMap(a->a,(a->a.getEnd_time().getTime()/1000-a.getStart_time().getTime()/1000)));
        try {
            fw=new FileWriter("durEachMonitData.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(Map.Entry<MonitoredData,Long> entry:durations.entrySet()){
            long hours=(entry.getValue()/3600)%24;
            long minutes=((entry.getValue()-hours*3600)/60)%60;
            long seconds=(entry.getValue()-hours*3600-minutes*60)%60;
            fw.append(entry.getKey()+" ->  Hours: "+hours+" ,Minutes: "+minutes+" ,Seconds : "+seconds+"\n");
        }
        fw.close();
        //global duration for each activity
        Map<String,Long> durationsGlobal=durations.entrySet().stream()
                .collect(Collectors.groupingBy(entry-> entry.getKey().getActivity(),Collectors.summingLong(entry->entry.getValue())));
        try {
            fw=new FileWriter("globalDurEachAct.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(Map.Entry<String,Long> entry:durationsGlobal.entrySet()){
            long hours=entry.getValue()/3600;
            long minutes=(entry.getValue()-hours*3600)/60;
            long seconds=entry.getValue()-hours*3600-minutes*60;
            fw.append(entry.getKey()+" ->  Hours: "+hours+" ,Minutes: "+minutes+" ,Seconds : "+seconds+"\n");
           // fw.append(entry.getKey()+" ->  "+entry.getValue()+"\n");
        }
        fw.close();
        //filter activities with 90% of monitoring records with dur < 5 min
        Map<String,Long> durationLess5Min= durations.entrySet().stream()
                .filter(entry->entry.getValue()< 300).collect(Collectors.groupingBy(a -> a.getKey().getActivity(),
                        Collectors.counting()));
        Map<String,Long> _90percent=durationLess5Min.entrySet().stream()
                .filter(entry->entry.getValue() >= 90*occurrences.get(entry.getKey())/100).collect(Collectors.toMap(entry->entry.getKey(),entry->entry.getValue()*100/occurrences.get(entry.getKey())));
        try {
            fw=new FileWriter("filter90per.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(Map.Entry entry:_90percent.entrySet()){
           fw.append(entry.getKey()+" : "+entry.getValue()+"%\n");
        }
        fw.close();

    }
}
