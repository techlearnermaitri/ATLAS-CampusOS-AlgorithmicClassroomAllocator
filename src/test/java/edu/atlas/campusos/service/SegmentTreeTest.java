package edu.atlas.campusos.service;
import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class SegmentTreeTest {
 @Test void usesHalfOpenRangesAndDetectsOverlap(){SegmentTree t=new SegmentTree();assertTrue(t.isFree(540,600));t.occupy(540,600);assertFalse(t.isFree(550,570));assertTrue(t.isFree(600,640));assertTrue(t.isFree(500,540));}
 @Test void rejectsInvalidRanges(){SegmentTree t=new SegmentTree();assertFalse(t.isFree(20,20));assertThrows(IllegalArgumentException.class,()->t.occupy(90,80));}
 @Test void freshTreeIsAvailableAcrossTheDay(){SegmentTree t=new SegmentTree();assertTrue(t.isFree(0,1440));}
 @Test void partialOverlapIsUnavailable(){SegmentTree t=new SegmentTree();t.occupy(600,660);assertFalse(t.isFree(650,700));}
 @Test void outOfDayQueryIsUnavailable(){SegmentTree t=new SegmentTree();assertFalse(t.isFree(-1,10));assertFalse(t.isFree(1430,1441));}
}
