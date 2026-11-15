package edu.atlas.campusos.service;

import org.springframework.stereotype.Service;import java.time.*;import java.util.*;

/** Reproducible synthetic scaling comparison; records never enter the university timetable. */
@Service
public class BenchmarkService {
    private static final int[] SIZES={50,200,800};private static final int ROOM_COUNT=48,REPEATS=3;private static final long SEED=20261115L;
    public Map<String,Object> run(){List<Map<String,Object>> results=new ArrayList<>();for(int size:SIZES){var data=generate(size);ClassroomAllocator.dsatur(data.sessions,data.rooms);ClassroomAllocator.firstFit(data.sessions,data.rooms);double[] ds=new double[REPEATS],ff=new double[REPEATS];int dAssigned=0,fAssigned=0;long conflicts=0;
            for(int i=0;i<REPEATS;i++){long t=System.nanoTime();var d=ClassroomAllocator.dsatur(data.sessions,data.rooms);ds[i]=(System.nanoTime()-t)/1_000_000.0;dAssigned=d.allocatedCount();conflicts=d.conflictPairs();t=System.nanoTime();var f=ClassroomAllocator.firstFit(data.sessions,data.rooms);ff[i]=(System.nanoTime()-t)/1_000_000.0;fAssigned=f.allocatedCount();}
            Map<String,Object> row=new LinkedHashMap<>();row.put("classes",size);row.put("rooms",ROOM_COUNT);row.put("conflictPairs",conflicts);row.put("dsaturAllocated",dAssigned);row.put("firstFitAllocated",fAssigned);row.put("dsaturMedianMs",median(ds));row.put("firstFitMedianMs",median(ff));row.put("dsaturCoverage",size==0?0:100.0*dAssigned/size);row.put("firstFitCoverage",size==0?0:100.0*fAssigned/size);results.add(row);}
        return Map.of("label","Synthetic benchmark (not university enrollment)","seed",SEED,"repeats",REPEATS,"rooms",ROOM_COUNT,"sizes",results,"methodology","Fixed seed; 48 synthetic rooms with capacities 30–120; class sizes 20–100; randomized start times and 45–90 minute intervals. One untimed warm-up per algorithm and size, then median of three measured runs. Same generated input is used for DSATUR and First-Fit. Timings depend on the machine.");}
    private Data generate(int n){Random random=new Random(SEED+n);List<ClassroomAllocator.Session> sessions=new ArrayList<>();for(int i=0;i<n;i++){int start=8*60+random.nextInt(8*60);int duration=45+random.nextInt(46);int end=Math.min(20*60,start+duration);int size=20+random.nextInt(81);sessions.add(new ClassroomAllocator.Session("SYN-"+(i+1),size,start,end));}List<ClassroomAllocator.Room> rooms=new ArrayList<>();for(int i=0;i<ROOM_COUNT;i++)rooms.add(new ClassroomAllocator.Room("SYN-R"+(i+1),30+random.nextInt(91),true));return new Data(sessions,rooms);}
    private double median(double[] values){Arrays.sort(values);return values[values.length/2];}
    private record Data(List<ClassroomAllocator.Session> sessions,List<ClassroomAllocator.Room> rooms){}
}
