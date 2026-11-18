package edu.atlas.campusos.service;
import org.junit.jupiter.api.Test;import java.util.*;import static org.junit.jupiter.api.Assertions.*;
class AllocationAlgorithmsTest {
 @Test void quickselectReturnsOddAndEvenMedians(){assertEquals(3,AllocationAlgorithms.median(List.of(1,3,5),Integer::doubleValue));assertEquals(2.5,AllocationAlgorithms.median(List.of(1,2,3,4),Integer::doubleValue));}
 @Test void quickselectHandlesEmptyAndSingleElementInputs(){assertEquals(0,AllocationAlgorithms.median(List.of(),Integer::doubleValue));assertEquals(8,AllocationAlgorithms.median(List.of(8),Integer::doubleValue));}
 @Test void quickselectFindsOddMedianWithoutSortedInput(){assertEquals(5,AllocationAlgorithms.median(List.of(9,1,5,3,7),Integer::doubleValue));}
 @Test void quickselectFindsEvenMedian(){assertEquals(6.0,AllocationAlgorithms.median(List.of(8,1,4,9),Integer::doubleValue));}
 @Test void quickselectHandlesRepeatedValues(){assertEquals(4,AllocationAlgorithms.median(List.of(4,4,4,2,9),Integer::doubleValue));}
}
