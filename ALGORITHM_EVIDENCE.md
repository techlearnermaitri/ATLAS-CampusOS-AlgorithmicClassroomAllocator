# CampusOS algorithm evidence

## Problem, objective, and constraints

Administrators enter real classrooms and scheduled classes. For one selected date, CampusOS assigns each valid session to at most one available room with sufficient seats, and prevents overlapping sessions from sharing a room. The operational objective is to make a useful feasible assignment quickly, expose classes that could not fit, and show how the DSATUR heuristic compares with input-order First-Fit. DSATUR is heuristic; it does not promise an optimal coloring.

The allocation engine treats times as integer minutes after midnight and intervals as half-open `[start,end)`. Thus 09:00–10:00 and 10:00–11:00 can share a room. Timetable validation rejects negative student counts, invalid program/year pairs, invalid dates or times, and end times that are not after start times. A valid session can still be unallocated because no available room is large enough or free for the entire interval.

Actual university enrollment is not inferred from school weights. The supplied weights 14, 65, 55, and 2 normalize to proportions over 136; they never become student counts in the allocation engine. No rooms or actual timetable entries are preloaded.

## Algorithms and correctness reasoning

### DSATUR graph coloring — primary allocator

1. Create one vertex per class and connect vertices whose half-open intervals overlap.
2. Repeatedly select an unprocessed vertex with the most distinct colors among already assigned neighbors; break ties by degree.
3. Scan available rooms by ascending capacity and assign the first room with enough seats and a free segment-tree interval.
4. Mark classes with no feasible room unallocated and continue, so one failure does not stop the report.

The room occupancy check enforces the edge constraint: when a vertex is assigned a room, every overlapping class sees that interval as occupied in that room's segment tree. Therefore two overlapping classes cannot receive the same room. The greedy selection and room choice can leave a class unallocated even when another assignment order would succeed, so the method is not an optimality guarantee. Graph construction takes `O(V²)` time and `O(V²)` space. This straightforward selection loop recomputes saturation while scanning vertices and neighbors on each selection, so DSATUR selection is `O(V³)` worst case here; room feasibility adds up to `O(VR log 1440)` query/update work for `R` rooms.

### Segment tree — per-room availability

Each room owns a segment tree over 1,440 minute leaves for a selected date. A range query asks whether the maximum occupancy in `[start,end)` is zero. A range update marks that interval occupied. Standard segment-tree range query/update work is `O(log N)` for this tree representation, where `N=1440`; the time axis is minute-granularity.

### Edmonds–Karp — modeled assignment ceiling

The flow graph has source → class nodes → feasible `(room, exact start, exact end)` nodes → sink. Unit capacities prevent two classes from consuming the same identical room/time slot. Edges include rooms that are available and large enough. BFS over residual adjacency lists finds augmenting paths; the implementation uses the Edmonds–Karp `O(VE²)` method.

This network does not encode atomic assignment across multiple minute slices. Two differently bounded intervals that partially overlap may map to different exact-slot nodes and appear simultaneously assignable. Treat its result as a model estimate, not proof that a complete timetable is feasible.

### Randomized Quickselect — CO5 median analysis

Randomized Quickselect selects the middle order statistic without fully sorting the input. CampusOS reports median class size and median room capacity. The expected time is `O(N)` and worst case `O(N²)`. An empty collection returns zero for display; it is not interpreted as an enrollment or capacity value.

### First-Fit — baseline

First-Fit processes sessions in input order and selects the first room satisfying capacity and segment-tree availability. It provides a simple baseline under the same room and time constraints. It is often faster, especially on small inputs; DSATUR may produce more assignments when ordering constraints matter, but neither method dominates for every timetable.

## Correctness test inventory

Run all tests with `mvn clean test`. Each assessed core algorithm has at least five named correctness cases, including edge or infeasible inputs.

| Algorithm | Test cases in the source |
|---|---|
| Segment tree | fresh day; occupied range; adjacent ranges; partial overlap; invalid/out-of-day range |
| DSATUR | empty input; adjacent classes reuse a room; overlapping classes separate and edge counted; no room fits; unavailable room; invalid interval |
| First-Fit | empty input; first feasible room; overlap prevents reuse; adjacent sessions reuse room; unavailable/undersized rooms; invalid interval |
| Edmonds–Karp | empty network; one feasible match; capacity-infeasible class; shared exact slot capacity; distinct rooms for one slot; unavailable room |
| Randomized Quickselect | odd/even medians; empty/singleton input; unsorted odd input; even input; repeated values |

The tests assert results and edge behavior, not a specific random pivot sequence.

## Reproducible benchmark

The **Analysis & reports → Run synthetic benchmark** action (and `GET /api/benchmark`) generates deterministic, synthetic workloads at 50, 200, and 800 classes. Each input uses 48 synthetic rooms. Class sizes range from 20–100, room capacities from 30–120, class starts from 08:00–15:59, and durations from 45–90 minutes. The fixed seed is `20261115`. Each algorithm gets one warm-up and three timed runs; the displayed duration is the median of those three runs. Both algorithms receive the exact same generated input. Data is labeled synthetic in the UI and is separate from administrator records; these values are not university enrollment data.

Run the benchmark tests with:

```bash
mvn -Dtest=BenchmarkServiceTest test
```

Observed example from the development machine (Java 25, Maven 3.9.12; elapsed milliseconds vary by hardware and system load):

| Synthetic classes | Conflict pairs | DSATUR assigned / median ms | First-Fit assigned / median ms |
|---:|---:|---:|---:|
| 50 | 333 | 50 / 0.846 | 50 / 0.435 |
| 200 | 5,240 | 200 / 14.766 | 199 / 3.039 |
| 800 | 82,413 | 319 / 478.734 | 296 / 4.598 |

On this workload, First-Fit is faster, while DSATUR assigned more classes on the two larger inputs. This is one deterministic synthetic scenario, not evidence that DSATUR always allocates more. The live benchmark view shows the run's own results so students can reproduce and discuss the behavior on their computer.

## Failure cases and known limits

- Invalid timetable rows are rejected with row numbers and reasons during import; valid rows in that file are imported and reported separately.
- Duplicate CSV records are rejected rather than silently overwritten.
- Empty timetable dates return a zero-class report.
- Oversized classes and occupied intervals remain unallocated with explicit reasons.
- DSATUR can produce a non-optimal assignment; assignment count and conflicts are exposed for interpretation.
- The flow estimate only reserves exact interval slots and may overestimate when intervals partially overlap.
- Benchmark records are synthetic and do not model actual student totals, room inventory, travel distance, course-specific room equipment, or instructor constraints.
