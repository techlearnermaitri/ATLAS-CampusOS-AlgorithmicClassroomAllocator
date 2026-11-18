package edu.atlas.campusos.service;

import java.util.*;

/** Input-order First-Fit baseline for comparing with maximum bipartite matching. */
public final class ClassroomAllocator {
    private ClassroomAllocator(){}
    public record Session(String id,int size,int startMinute,int endMinute){}
    public record Room(String name,int capacity,boolean available){}
    public record Result(int[] roomBySession){public int allocatedCount(){int n=0;for(int r:roomBySession)if(r>=0)n++;return n;}}
    public static Result firstFit(List<Session> sessions,List<Room> rooms){
        List<Room> available=rooms.stream().filter(Room::available).toList();Map<Integer,SegmentTree> occupied=new HashMap<>();for(int r=0;r<available.size();r++)occupied.put(r,new SegmentTree());int[] assigned=new int[sessions.size()];Arrays.fill(assigned,-1);
        for(int i=0;i<sessions.size();i++){Session s=sessions.get(i);if(!valid(s))continue;for(int r=0;r<available.size();r++){Room room=available.get(r);SegmentTree tree=occupied.get(r);if(room.capacity>=s.size&&tree.isFree(s.startMinute,s.endMinute)){assigned[i]=r;tree.occupy(s.startMinute,s.endMinute);break;}}}
        return new Result(assigned);
    }
    private static boolean valid(Session s){return s!=null&&s.size>=0&&s.startMinute>=0&&s.endMinute<=1440&&s.startMinute<s.endMinute;}
}
