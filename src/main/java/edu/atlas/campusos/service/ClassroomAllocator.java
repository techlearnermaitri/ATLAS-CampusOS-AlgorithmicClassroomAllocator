package edu.atlas.campusos.service;

import java.util.*;

/** DSATUR graph coloring and input-order First-Fit for the same room/time constraints. */
public final class ClassroomAllocator {
    private ClassroomAllocator() {}
    public record Session(String id,int size,int startMinute,int endMinute) {}
    public record Room(String name,int capacity,boolean available) {}
    public record Result(int[] roomBySession,long conflictPairs) {
        public int allocatedCount(){int n=0;for(int room:roomBySession)if(room>=0)n++;return n;}
    }

    /** Greedy DSATUR: choose by saturation, then graph degree, and use best-fit feasible room. */
    public static Result dsatur(List<Session> sessions,List<Room> rooms){
        int n=sessions.size();boolean[][] edges=new boolean[n][n];int[] degree=new int[n];long pairs=0;
        for(int i=0;i<n;i++)for(int j=i+1;j<n;j++)if(overlap(sessions.get(i),sessions.get(j))){edges[i][j]=edges[j][i]=true;degree[i]++;degree[j]++;pairs++;}
        List<Room> available=rooms.stream().filter(Room::available).sorted(Comparator.comparingInt(Room::capacity)).toList();
        Map<Integer,SegmentTree> occupancy=new HashMap<>();for(int i=0;i<available.size();i++)occupancy.put(i,new SegmentTree());
        int[] assigned=new int[n];Arrays.fill(assigned,-1);boolean[] processed=new boolean[n];
        for(int step=0;step<n;step++){
            int pick=-1,bestSaturation=-1,bestDegree=-1;
            for(int i=0;i<n;i++)if(!processed[i]){BitSet colors=new BitSet();for(int j=0;j<n;j++)if(edges[i][j]&&assigned[j]>=0)colors.set(assigned[j]);int saturation=colors.cardinality();if(saturation>bestSaturation||(saturation==bestSaturation&&degree[i]>bestDegree)){pick=i;bestSaturation=saturation;bestDegree=degree[i];}}
            processed[pick]=true;Session s=sessions.get(pick);if(!valid(s))continue;
            for(int r=0;r<available.size();r++){Room room=available.get(r);SegmentTree tree=occupancy.get(r);if(room.capacity>=s.size&&tree.isFree(s.startMinute,s.endMinute)){assigned[pick]=r;tree.occupy(s.startMinute,s.endMinute);break;}}
        }
        return new Result(assigned,pairs);
    }

    /** Simple baseline: process sessions in supplied order and take the first feasible room. */
    public static Result firstFit(List<Session> sessions,List<Room> rooms){
        List<Room> available=rooms.stream().filter(Room::available).toList();Map<Integer,SegmentTree> occupancy=new HashMap<>();for(int i=0;i<available.size();i++)occupancy.put(i,new SegmentTree());int[] assigned=new int[sessions.size()];Arrays.fill(assigned,-1);
        for(int i=0;i<sessions.size();i++){Session s=sessions.get(i);if(!valid(s))continue;for(int r=0;r<available.size();r++){Room room=available.get(r);SegmentTree tree=occupancy.get(r);if(room.capacity>=s.size&&tree.isFree(s.startMinute,s.endMinute)){assigned[i]=r;tree.occupy(s.startMinute,s.endMinute);break;}}}
        return new Result(assigned,countConflicts(sessions));
    }
    public static long countConflicts(List<Session> sessions){long count=0;for(int i=0;i<sessions.size();i++)for(int j=i+1;j<sessions.size();j++)if(overlap(sessions.get(i),sessions.get(j)))count++;return count;}
    private static boolean valid(Session s){return s!=null&&s.size>=0&&s.startMinute>=0&&s.endMinute<=1440&&s.startMinute<s.endMinute;}
    private static boolean overlap(Session a,Session b){return valid(a)&&valid(b)&&a.startMinute<b.endMinute&&b.startMinute<a.endMinute;}
}
