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
