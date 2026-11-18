# CampusOS algorithm evidence

## Matching problem

For each configured period on a selected date, classes are requests on the left side of a bipartite graph and available classrooms are vertices on the right. An edge exists when a room is available, has enough seats, and is not already occupied in that period. A matching assigns each class to at most one room and each room to at most one simultaneous request.

CampusOS computes a maximum-cardinality matching with augmenting paths. For each left vertex, depth-first search follows feasible edges and can reassign an earlier match when that enables a larger matching. The resulting matching is maximum for the supplied bipartite graph. The standard augmenting-path implementation is `O(VE)` in the general bipartite graph representation used here. It does not prove globally optimality for arbitrary overlapping time intervals, nor for constraints omitted from the graph.

Booking tiers are applied explicitly: classes are matched first; confirmed event requests use remaining rooms next; study requests use the remaining rooms last. Booking forms accept direct start and end times without requiring a timetable-period name. Segment-tree availability checks use the exact requested interval, including partial overlaps with classes and earlier bookings. The matcher tries a preferred floor first, then falls back to the smallest free suitable room on another floor when necessary. Events can be a single room or a whole floor. The event preview calculates room/floor alternatives and compares class matching before and after a proposed whole-floor closure. Study requests are admin-managed and receive an allocation or waitlist outcome when matching is run.

## Configured time periods and segment trees

Administrators define non-overlapping period intervals. A class must use the exact start and end times of an active period; otherwise its result is Invalid with a clear explanation. Each room has its own segment tree with 1,440 minute leaves. Availability queries and range occupancy updates are `O(log N)` for this representation. Intervals use half-open `[start,end)` semantics, so a period ending at 10:00 does not overlap one starting at 10:00. The app uses these periods to form independent room matching slots.

## First-Fit baseline

First-Fit processes requests in input order and takes the first available room satisfying capacity and occupancy constraints. It provides a simple baseline for the analysis benchmark. It may produce fewer assignments than maximum matching when request choices differ, but execution time and matching count depend on the input.

## Randomized Quickselect

Quickselect selects the middle order statistic without sorting all values. CampusOS uses it for median class size and median room capacity from the actual records in the selected report view. Expected time is `O(N)`; worst case is `O(N²)`. Empty inputs display zero as a UI value and do not imply actual enrollment.

## Benchmark

The Analysis & reports page generates fixed-seed synthetic inputs (50, 200, and 800 requests; 48 synthetic rooms). Every request competes in one period, with the same generated graph passed to matching and First-Fit. Each algorithm gets one warm-up; the displayed time is the median of three runs. Synthetic data is separate from university timetable and enrollment records. Runtime values vary by machine.

## Test coverage

Run with `mvn clean test`.

- Bipartite matching: empty sides, no feasible edge, one-to-one matches, competing requests, augmenting-path reassignment, and maximum cardinality.
- Segment tree: free range, occupied range, partial overlap, adjacent ranges, and invalid bounds.
- First-Fit: empty input, feasible room selection, capacity and availability constraints, overlap prevention, and adjacent sessions.
- Quickselect: odd/even medians, empty/singleton collections, unsorted inputs, and repeated values.
- Benchmark: result bounds and both measured algorithm outputs.

## Limits and interpretation

Matching optimizes the number of assignments for the modeled graph, not travel distance, room preferences beyond configured floor constraints, equipment needs, or arbitrary partial-overlap scheduling across mismatched periods. Configured periods must be non-overlapping. Event preview is a planning estimate based on current timetable and bookings; rerun allocation after changes to persist assignments. A floor-wide event is treated as reserving all available classrooms on that floor. Seat utilization is based on assigned headcounts relative to available room seats across the reporting periods and should not be interpreted as seat-hours.
