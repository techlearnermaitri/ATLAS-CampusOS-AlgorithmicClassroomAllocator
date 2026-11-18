package edu.atlas.campusos.service;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class MaximumBipartiteMatcherTest {
 @Test void emptyGraphHasNoMatches(){assertEquals(0,MaximumBipartiteMatcher.match(new boolean[0][0]).cardinality());}
 @Test void matchesSingleFeasiblePair(){var r=MaximumBipartiteMatcher.match(new boolean[][]{{true}});assertEquals(1,r.cardinality());assertEquals(0,r.rightByLeft()[0]);}
 @Test void leavesRequestUnmatchedWhenItHasNoFeasibleRoom(){var r=MaximumBipartiteMatcher.match(new boolean[][]{{false,true},{false,false}});assertEquals(1,r.cardinality());assertEquals(-1,r.rightByLeft()[1]);}
 @Test void findsMaximumCardinalityAcrossCompetingRequests(){var r=MaximumBipartiteMatcher.match(new boolean[][]{{true,false},{true,false},{false,true}});assertEquals(2,r.cardinality());}
 @Test void augmentingPathReroutesEarlierMatch(){var r=MaximumBipartiteMatcher.match(new boolean[][]{{true,true},{true,false}});assertEquals(2,r.cardinality());assertEquals(1,r.rightByLeft()[0]);assertEquals(0,r.rightByLeft()[1]);}
 @Test void rejectsRaggedAdjacencyMatrix(){assertThrows(IllegalArgumentException.class,()->MaximumBipartiteMatcher.match(new boolean[][]{{true,false},{true}}));}
}
