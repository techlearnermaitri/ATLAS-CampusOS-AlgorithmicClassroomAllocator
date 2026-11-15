package edu.atlas.campusos.service;
import org.junit.jupiter.api.Test;import java.util.*;import static org.junit.jupiter.api.Assertions.*;
class AllocationAlgorithmsTest {
 @Test void quickselectReturnsOddAndEvenMedians(){assertEquals(3,AllocationAlgorithms.median(List.of(1,3,5),Integer::doubleValue));assertEquals(2.5,AllocationAlgorithms.median(List.of(1,2,3,4),Integer::doubleValue));}
 @Test void flowSharesIdenticalRoomTimeSlotCapacity(){var classes=List.of(new AllocationAlgorithms.FlowClass(20,"09:00","10:00"),new AllocationAlgorithms.FlowClass(20,"09:00","10:00"));var rooms=List.of(new AllocationAlgorithms.FlowRoom("A",30,true));assertEquals(1,AllocationAlgorithms.flowEstimate(classes,rooms).possible());}
 @Test void quickselectHandlesEmptyAndSingleElementInputs(){assertEquals(0,AllocationAlgorithms.median(List.of(),Integer::doubleValue));assertEquals(8,AllocationAlgorithms.median(List.of(8),Integer::doubleValue));}
 @Test void quickselectFindsOddMedianWithoutSortedInput(){assertEquals(5,AllocationAlgorithms.median(List.of(9,1,5,3,7),Integer::doubleValue));}
 @Test void quickselectFindsEvenMedian(){assertEquals(6.0,AllocationAlgorithms.median(List.of(8,1,4,9),Integer::doubleValue));}
 @Test void quickselectHandlesRepeatedValues(){assertEquals(4,AllocationAlgorithms.median(List.of(4,4,4,2,9),Integer::doubleValue));}
 @Test void flowHandlesEmptyNetwork(){assertEquals(0,AllocationAlgorithms.flowEstimate(List.of(),List.of()).possible());}
 @Test void flowAssignsOneFeasibleClass(){var f=AllocationAlgorithms.flowEstimate(List.of(new AllocationAlgorithms.FlowClass(20,"09:00","10:00")),List.of(new AllocationAlgorithms.FlowRoom("A",30,true)));assertEquals(1,f.possible());}
 @Test void flowRejectsCapacityInfeasibleClass(){var f=AllocationAlgorithms.flowEstimate(List.of(new AllocationAlgorithms.FlowClass(40,"09:00","10:00")),List.of(new AllocationAlgorithms.FlowRoom("A",30,true)));assertEquals(0,f.possible());}
 @Test void flowCanUseDistinctRoomsForSharedSlot(){var classes=List.of(new AllocationAlgorithms.FlowClass(20,"09:00","10:00"),new AllocationAlgorithms.FlowClass(20,"09:00","10:00"));var rooms=List.of(new AllocationAlgorithms.FlowRoom("A",30,true),new AllocationAlgorithms.FlowRoom("B",30,true));assertEquals(2,AllocationAlgorithms.flowEstimate(classes,rooms).possible());}
 @Test void flowExcludesUnavailableRooms(){var f=AllocationAlgorithms.flowEstimate(List.of(new AllocationAlgorithms.FlowClass(20,"09:00","10:00")),List.of(new AllocationAlgorithms.FlowRoom("A",30,false)));assertEquals(0,f.possible());}
}
