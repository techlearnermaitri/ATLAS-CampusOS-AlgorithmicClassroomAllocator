package edu.atlas.campusos.service;
import org.junit.jupiter.api.Test;import java.util.*;import static org.junit.jupiter.api.Assertions.*;
class ClassroomAllocatorTest {
 private ClassroomAllocator.Session s(String id,int size,int from,int to){return new ClassroomAllocator.Session(id,size,from,to);}
 private ClassroomAllocator.Room r(String name,int capacity,boolean available){return new ClassroomAllocator.Room(name,capacity,available);}

 @Test void dsaturHandlesEmptyInput(){assertEquals(0,ClassroomAllocator.dsatur(List.of(),List.of(r("A",30,true))).allocatedCount());}
 @Test void dsaturReusesRoomForNonOverlappingClasses(){var out=ClassroomAllocator.dsatur(List.of(s("a",20,540,600),s("b",20,600,660)),List.of(r("A",30,true)));assertEquals(2,out.allocatedCount());assertEquals(out.roomBySession()[0],out.roomBySession()[1]);}
 @Test void dsaturSeparatesOverlappingClassesAndCountsEdge(){var out=ClassroomAllocator.dsatur(List.of(s("a",20,540,600),s("b",20,570,630)),List.of(r("A",30,true),r("B",30,true)));assertEquals(2,out.allocatedCount());assertNotEquals(out.roomBySession()[0],out.roomBySession()[1]);assertEquals(1,out.conflictPairs());}
 @Test void dsaturLeavesClassUnallocatedWhenNoRoomFits(){var out=ClassroomAllocator.dsatur(List.of(s("a",50,540,600)),List.of(r("A",30,true)));assertEquals(-1,out.roomBySession()[0]);}
 @Test void dsaturRejectsUnavailableRoom(){var out=ClassroomAllocator.dsatur(List.of(s("a",20,540,600)),List.of(r("A",30,false)));assertEquals(0,out.allocatedCount());}
 @Test void dsaturSkipsInvalidTimeRange(){var out=ClassroomAllocator.dsatur(List.of(s("bad",20,600,540)),List.of(r("A",30,true)));assertEquals(0,out.allocatedCount());}

 @Test void firstFitHandlesEmptyInput(){assertEquals(0,ClassroomAllocator.firstFit(List.of(),List.of(r("A",30,true))).allocatedCount());}
 @Test void firstFitUsesFirstFeasibleRoomInOrder(){var out=ClassroomAllocator.firstFit(List.of(s("a",20,540,600)),List.of(r("first",30,true),r("second",50,true)));assertEquals(0,out.roomBySession()[0]);}
 @Test void firstFitDoesNotReuseOccupiedRoomForOverlap(){var out=ClassroomAllocator.firstFit(List.of(s("a",20,540,600),s("b",20,570,630)),List.of(r("A",30,true)));assertEquals(1,out.allocatedCount());}
 @Test void firstFitReusesRoomForAdjacentIntervals(){var out=ClassroomAllocator.firstFit(List.of(s("a",20,540,600),s("b",20,600,660)),List.of(r("A",30,true)));assertEquals(2,out.allocatedCount());}
 @Test void firstFitSkipsUnavailableAndUndersizedRooms(){var out=ClassroomAllocator.firstFit(List.of(s("a",40,540,600)),List.of(r("closed",80,false),r("small",30,true)));assertEquals(0,out.allocatedCount());}
 @Test void firstFitSkipsInvalidTimeRange(){var out=ClassroomAllocator.firstFit(List.of(s("bad",20,600,540)),List.of(r("A",30,true)));assertEquals(0,out.allocatedCount());}
}
