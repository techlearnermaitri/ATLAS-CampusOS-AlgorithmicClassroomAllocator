package edu.atlas.campusos.service;

import org.springframework.stereotype.Service;import java.util.*;

/** Deterministic one-period synthetic comparison, separate from persisted campus data. */
@Service
public class BenchmarkService {
    private static final int[] SIZES={50,200,800};private static final int ROOM_COUNT=48,REPEATS=3;private static final long SEED=20261115L;
    public Map<String,Object> run(){List<Map<String,Object>> rows=new ArrayList<>();for(int n:SIZES){Data d=generate(n);boolean[][] graph=graph(d.sessions,d.rooms);MaximumBipartiteMatcher.match(graph);ClassroomAllocator.firstFit(d.sessions,d.rooms);double[] mt=new double[REPEATS],ft=new double[REPEATS];int matched=0,first=0;
        for(int i=0;i<REPEATS;i++){long t=System.nanoTime();var m=MaximumBipartiteMatcher.match(graph);mt[i]=(System.nanoTime()-t)/1_000_000.0;matched=m.cardinality();t=System.nanoTime();var f=ClassroomAllocator.firstFit(d.sessions,d.rooms);ft[i]=(System.nanoTime()-t)/1_000_000.0;first=f.allocatedCount();}
        Map<String,Object> row=new LinkedHashMap<>();row.put("classes",n);row.put("rooms",ROOM_COUNT);row.put("bipartiteMatched",matched);row.put("firstFitMatched",first);row.put("bipartiteMedianMs",median(mt));row.put("firstFitMedianMs",median(ft));row.put("bipartiteCoverage",100.0*matched/n);row.put("firstFitCoverage",100.0*first/n);rows.add(row);}
        return Map.of("label","Synthetic one-period benchmark (not university enrollment)","seed",SEED,"repeats",REPEATS,"rooms",ROOM_COUNT,"sizes",rows,"methodology","Fixed seed; all requests compete in one synthetic period; 48 synthetic rooms with capacities 30–120; class sizes 20–100. One warm-up per algorithm and size, then median of three measured runs on the same input. Timing depends on the computer.");}
    private boolean[][] graph(List<ClassroomAllocator.Session> sessions,List<ClassroomAllocator.Room> rooms){boolean[][] e=new boolean[sessions.size()][rooms.size()];for(int i=0;i<sessions.size();i++)for(int j=0;j<rooms.size();j++)e[i][j]=rooms.get(j).available()&&rooms.get(j).capacity()>=sessions.get(i).size();return e;}
    private Data generate(int n){Random random=new Random(SEED+n);List<ClassroomAllocator.Session> sessions=new ArrayList<>();for(int i=0;i<n;i++)sessions.add(new ClassroomAllocator.Session("SYN-"+(i+1),20+random.nextInt(81),540,600));List<ClassroomAllocator.Room> rooms=new ArrayList<>();for(int i=0;i<ROOM_COUNT;i++)rooms.add(new ClassroomAllocator.Room("SYN-R"+(i+1),30+random.nextInt(91),true));return new Data(sessions,rooms);}
    private double median(double[] a){Arrays.sort(a);return a[a.length/2];}
    private record Data(List<ClassroomAllocator.Session> sessions,List<ClassroomAllocator.Room> rooms){}
}
