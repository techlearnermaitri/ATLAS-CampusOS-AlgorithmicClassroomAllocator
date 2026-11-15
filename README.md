# 🏫 CampusOS

### Classroom planning and allocation for ATLAS SKILLTECH University

CampusOS helps university administrators manage classrooms and timetables, then automatically assign scheduled classes to available rooms. It brings algorithm-based allocation, capacity checks, and utilization reporting into one focused application.

> **Project scope:** Classroom planning and allocation. CampusOS is not a university ERP.

![Java](https://img.shields.io/badge/Java-17%2B-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F)
![License](https://img.shields.io/badge/License-Add%20your%20license-lightgrey)

---

## ✨ Features

- **Academic structure** — Browse UGDX, ISME, ISDI, and LAW programs; manage divisions and enter actual student counts.
- **Classroom inventory** — Add, edit, search, and manage rooms across seven floors, including capacity and availability.
- **Timetable management** — Create and filter scheduled classes, or import them from CSV.
- **Automatic allocation** — Run DSATUR-based classroom allocation for a selected date.
- **Allocation results** — Review assigned rooms, floors, unallocated classes, and reasons.
- **Analysis and reports** — View coverage, seat utilization, scheduling conflicts, room usage, and bottlenecks.
- **Algorithm benchmark** — Compare DSATUR and First-Fit on reproducible synthetic workloads.

## 🧠 Algorithms

CampusOS implements its assessed algorithms in Java without using libraries to perform the core algorithmic work.

| Algorithm | How CampusOS uses it |
|---|---|
| **DSATUR graph coloring** | Prioritizes classes by saturation degree and graph degree, then assigns a feasible room. It is a heuristic and does not guarantee an optimal timetable. |
| **Segment tree** | Tracks occupied minutes independently for each room. Half-open intervals `[start, end)` allow back-to-back classes. Range availability queries and updates are `O(log N)`. |
| **Edmonds–Karp** | Estimates possible assignments using a source, class nodes, feasible room and exact-time-slot nodes, and a sink. Complexity: `O(VE²)`. Partial overlaps across different interval boundaries are not encoded, so the result is an estimate—not a feasibility guarantee. |
| **Randomized Quickselect** | Calculates median class size and room capacity. Expected time: `O(N)`; worst case: `O(N²)`. |
| **First-Fit** | Provides an input-order baseline using the same capacity and availability constraints. |

See [`ALGORITHM_EVIDENCE.md`](ALGORITHM_EVIDENCE.md) for test cases, correctness notes, assumptions, limitations, and benchmark results.

## 🏗️ Built with

- Java 17+
- Spring Boot 3.3.5
- Spring MVC and Spring Data JPA
- H2 local database
- HTML, CSS, and JavaScript
- Maven

## 🚀 Run locally

### Requirements

- Java 17 or newer
- Maven 3.8 or newer

### Start the application

From the project folder containing `pom.xml`:

```bash
mvn spring-boot:run 
```




Open http://localhost:8081.

## 🧪 Try the timetable workflow
1. Start CampusOS and add classroom records with their real floor, capacity, and availability.
2. Open CSV upload and download the blank template or the synthetic demo timetable.
3. The demo records use SYN-DEMO IDs and fabricated student counts. They are test inputs, not actual ATLAS enrollment.
4. Upload a timetable CSV and review the import summary. Invalid rows show their row numbers and reasons.
5. Open Allocation, choose the timetable date, and select Run Allocation.
6. Review assignments and bottlenecks in Allocation results and Analysis & reports.
The included demo timetable is located at:
src/main/resources/static/sample-data/synthetic-timetable.csv
📄 CSV format
Required columns:
classId,school,program,academicYear,division,studentCount,date,startTime,endTime
Use the school codes UGDX, ISME, ISDI, or LAW; an exact program name from that school’s catalog; ISO dates (YYYY-MM-DD); and 24-hour times (HH:mm).

CampusOS reports rejected records rather than silently ignoring them. Valid rows can be imported even if other rows in the file are rejected.
🏫 Academic assumptions
The supplied school weights are relative values, not percentages or enrollment counts:
School	Weight
UGDX	14
ISME	65
ISDI	55
LAW	2


CampusOS normalizes each weight by the total, 136, to display estimated proportions. It does not convert those proportions into student counts. Administrators can enter actual enrollment later. The approximately ten divisions per academic year figure is a planning assumption only; no divisions are generated automatically.
💾 Storage
By default, CampusOS stores data in a local H2 database at ./data/campusos. The database is created when the application starts and persists across restarts. No actual classrooms, timetable records, or enrollment counts are preloaded.

Firebase is not connected in this version. See [`FIREBASE.md`](FIREBASE.md) for the planned setup path.
📁 Project layout

```
CampusOS/
├── src/
│   ├── main/
│   │   ├── java/edu/atlas/campusos/
│   │   │   ├── model/       # Database entities
│   │   │   ├── repo/        # Spring Data repositories
│   │   │   ├── service/     # Allocation and analysis algorithms
│   │   │   └── web/         # REST API and request handling
│   │   └── resources/
│   │       ├── static/      # Frontend and sample CSV
│   │       └── application.properties
│   └── test/                # Algorithm correctness tests
├── ALGORITHM_EVIDENCE.md
├── FIREBASE.md
├── README.md
└── pom.xml
```
