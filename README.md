# CampusOS

CampusOS is a focused classroom planning and allocation application for ATLAS SKILLTECH University. It manages academic structure, real classroom inventory, timetable records, CSV imports, automatic allocations, and utilization reports. It does not include unrelated university ERP modules.

## Requirements

- Java 17 or newer
- Maven 3.8 or newer
- Network access the first time Maven resolves project dependencies

## Run locally

From this folder:

```bash
mvn spring-boot:run
```

Open [http://localhost:8081](http://localhost:8081). CampusOS uses port 8081 by default to avoid the common port 8080 conflict. If 8081 is also busy, start it on another port with `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8082` and open `http://localhost:8082`.

To run the automated tests:

```bash
mvn clean test
```

Spring Boot runs from the Maven plugin goal `spring-boot:run` (the colon is required by Maven's goal syntax).

## Local storage

By default the app stores data in a local H2 database at `./data/campusos`. It is created when the app starts and survives restarts. No actual student enrollment or classroom inventory is pre-filled. The four requested schools and their programs are built in; division counts and room information are administrator-entered.

## First steps

1. Add actual classrooms, floors, capacity, and availability in **Classrooms**.
2. Add divisions and actual student counts when known in **Academic structure**. Blank actual enrollment stays blank.
3. Add class sessions manually or download the CSV template and upload timetable records.
4. Open **Allocation**, select a date, and choose **Run Allocation**. The app asks before replacing saved results for that date.
5. Review unallocated reasons in **Allocation results** and capacity, conflict, coverage, and usage summaries in **Analysis & reports**.

The supplied relative school weights total 136 and display as normalized proportions. The planning assumption of approximately ten divisions per academic year is explanatory only; it is not generated as actual divisions or enrollment.

The upload area also offers a clearly labeled **synthetic demo timetable** so you can try the workflow without university enrollment records. Its `SYN-DEMO` rows and student counts are fabricated test inputs, not actual or estimated ATLAS enrollment. The benchmark generator uses separate synthetic data and never writes it into the timetable.

## CSV format

The downloadable template contains the required header row:

```text
classId,school,program,academicYear,division,studentCount,date,startTime,endTime
```

Use school codes `UGDX`, `ISME`, `ISDI`, or `LAW`; an exact program name from that school's catalog; ISO dates (`YYYY-MM-DD`); and 24-hour times (`HH:mm`). Each invalid non-empty row is reported with its row number. Valid rows in the same file are imported and the rejected rows are listed; duplicate records are rejected.

## Allocation and analysis notes

- DSATUR builds a graph with classes as vertices and overlapping intervals as edges. It chooses the next unassigned class by saturation degree, then graph degree, and greedily chooses a room. Graph coloring is a heuristic and does not guarantee an optimal result.
- Each room has a minute-based segment tree for a calendar day. Intervals are half-open `[start,end)`, so back-to-back sessions do not conflict. Range availability and occupancy update are `O(log N)`.
- Edmonds–Karp uses BFS over class nodes and room plus exact-interval nodes. An identical room/time slot has capacity one. Partial overlaps with different interval boundaries are not encoded, so this is a comparison estimate, not a guarantee of a feasible full timetable. Complexity is `O(VE²)`.
- Randomized Quickselect calculates medians of class sizes and room capacities. Expected time is `O(N)` and worst case `O(N²)`.
- First-Fit allocates in input order and uses the same room availability and capacity checks as a baseline.
- Utilization is reported as allocated students divided by available seats in the selected reporting view. It is a seat utilization measure, not seat-hours.

The **Analysis & reports** page has a repeatable synthetic benchmark comparing DSATUR with First-Fit at 50, 200, and 800 classes. It reports assignment counts and median execution time for both methods. See [ALGORITHM_EVIDENCE.md](ALGORITHM_EVIDENCE.md) for algorithm explanations, the test inventory, benchmark inputs, and observed results.

## Firebase storage

The app currently uses H2 so it starts locally without cloud credentials. See [FIREBASE.md](FIREBASE.md) for the safe setup path and the details needed to connect your Firebase project. Do not put a Firebase service-account key in this ZIP or commit it to source control.
