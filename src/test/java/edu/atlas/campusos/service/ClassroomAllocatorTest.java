package edu.atlas.campusos.service;
import org.junit.jupiter.api.Test;import java.util.*;import static org.junit.jupiter.api.Assertions.*;
class ClassroomAllocatorTest {
 private ClassroomAllocator.Session s(String id,int n,int start,int end){return new ClassroomAllocator.Session(id,n,start,end);}private ClassroomAllocator.Room r(String name,int seats,boolean open){return new ClassroomAllocator.Room(name,seats,open);}
 @Test void firstFitHandlesEmptyInput(){assertEquals(0,ClassroomAllocator.firstFit(List.of(),List.of(r("A",30,true))).allocatedCount());}
 @Test void firstFitUsesFirstSuitableAvailableRoom(){assertEquals(0,ClassroomAllocator.firstFit(List.of(s("a",20,540,600)),List.of(r("first",30,true),r("second",50,true))).roomBySession()[0]);}
 @Test void firstFitCannotReuseRoomInSamePeriod(){assertEquals(1,ClassroomAllocator.firstFit(List.of(s("a",20,540,600),s("b",20,540,600)),List.of(r("A",30,true))).allocatedCount());}
 @Test void firstFitCanReuseRoomForDisjointPeriods(){assertEquals(2,ClassroomAllocator.firstFit(List.of(s("a",20,540,600),s("b",20,600,660)),List.of(r("A",30,true))).allocatedCount());}
 @Test void firstFitSkipsClosedAndUndersizedRooms(){assertEquals(0,ClassroomAllocator.firstFit(List.of(s("a",40,540,600)),List.of(r("closed",80,false),r("small",30,true))).allocatedCount());}
}
