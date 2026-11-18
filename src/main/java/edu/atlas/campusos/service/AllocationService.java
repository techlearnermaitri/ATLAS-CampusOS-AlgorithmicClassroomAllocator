package edu.atlas.campusos.service;

import edu.atlas.campusos.model.*;
import edu.atlas.campusos.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class AllocationService {
    private final RoomRepository rooms;
    private final SessionRepository sessions;
    private final AllocationRepository allocations;
    private final TimePeriodRepository periods;
    private final SpaceRequestRepository requests;
    private final SpaceAssignmentRepository spaceAssignments;

    public AllocationService(RoomRepository rooms, SessionRepository sessions, AllocationRepository allocations,
                             TimePeriodRepository periods, SpaceRequestRepository requests,
                             SpaceAssignmentRepository spaceAssignments) {
        this.rooms = rooms;
        this.sessions = sessions;
        this.allocations = allocations;
        this.periods = periods;
        this.requests = requests;
        this.spaceAssignments = spaceAssignments;
    }

    @Transactional
    public Map<String, Object> run(LocalDate date, boolean replace) {
        long started = System.nanoTime();
        if ((!allocations.findByDate(date).isEmpty() || !spaceAssignments.findByDate(date).isEmpty()) && !replace)
            throw new IllegalStateException("Results already exist for this date. Confirm replacement to rerun matching.");
        if (replace) {
            allocations.deleteByDate(date);
            spaceAssignments.deleteByDate(date);
        }

        List<Room> allRooms = sortedRooms();
        List<TimePeriod> slots = activePeriods();
        List<ClassSession> day = sessions.findByDate(date);
        List<SpaceRequest> dayRequests = requests.findByDateOrderByCreatedAt(date);
        Map<Long, SegmentTree> trees = newTrees(allRooms);
        List<Allocation> classResults = new ArrayList<>();
        List<SpaceAssignment> requestResults = new ArrayList<>();
        Set<Long> mapped = new HashSet<>();
        long matchedClasses = 0;

        // Classes are matched first in their configured timetable periods.
        for (TimePeriod slot : slots) {
            List<ClassSession> slotClasses = day.stream().filter(c -> sameTime(c, slot)).toList();
            mapped.addAll(slotClasses.stream().map(c -> c.id).toList());
            MaximumBipartiteMatcher.Result matching = matchClasses(slotClasses, allRooms, trees, Set.of());
            int[] roomByClass = matching.rightByLeft();
            matchedClasses += matching.cardinality();
            for (int i = 0; i < slotClasses.size(); i++) {
                ClassSession c = slotClasses.get(i);
                Allocation a = classAllocation(c, slot);
                if (roomByClass[i] >= 0) {
                    Room room = allRooms.get(roomByClass[i]);
                    a.status = "Allocated";
                    a.assignedRoom = room.name;
                    a.floor = room.floor;
                    trees.get(room.id).occupy(minute(c.startTime), minute(c.endTime));
                } else {
                    a.status = "Unallocated";
                    List<Room> fitting = allRooms.stream().filter(r -> r.available && r.capacity >= c.studentCount).toList();
                    if (fitting.isEmpty()) a.reason = "Needs " + c.studentCount + " seats; no available classroom has sufficient capacity.";
                    else {
                        List<String> blockers = classResults.stream().filter(x -> "Allocated".equals(x.status) && Objects.equals(x.periodId, a.periodId) && fitting.stream().anyMatch(r -> r.name.equals(x.assignedRoom))).map(x -> x.classId + " in " + x.assignedRoom).distinct().toList();
                        a.reason = "Needs " + c.studentCount + " seats; " + fitting.size() + " available room" + (fitting.size()==1?"":"s") + " fit. " + (blockers.isEmpty() ? "They are occupied during this interval." : "Suitable room" + (blockers.size()==1?" is":"s are") + " occupied by " + String.join(", ", blockers) + ".");
                    }
                }
                classResults.add(a);
            }
        }

        // Rows whose class times do not map to an active timetable period remain visible as Invalid.
        for (ClassSession c : day) {
            if (!mapped.contains(c.id)) {
                Allocation a = classAllocation(c, null);
                a.status = "Invalid";
                a.reason = "Class time does not match an active configured period.";
                classResults.add(a);
            }
        }

        // Whole-floor events reserve every available room on the selected floor.
        List<SpaceRequest> floorEvents = dayRequests.stream()
                .filter(r -> "EVENT".equals(r.type) && r.floorWide && isActiveRequest(r))
                .toList();
        for (SpaceRequest event : floorEvents) requestResults.add(assignFloorEvent(event, allRooms, trees));

        // Confirmed events get priority over study groups. A time range can cut across class periods.
        List<SpaceRequest> roomEvents = dayRequests.stream()
                .filter(r -> "EVENT".equals(r.type) && !r.floorWide && isActiveRequest(r)).toList();
        for (SpaceRequest event : roomEvents) requestResults.add(assignSmallestRoom(event, allRooms, trees));
        List<SpaceRequest> studyRequests = dayRequests.stream()
                .filter(r -> "STUDY".equals(r.type) && !"CANCELLED".equals(r.status)).toList();
        for (SpaceRequest study : studyRequests) requestResults.add(assignSmallestRoom(study, allRooms, trees));

        double elapsed = (System.nanoTime() - started) / 1_000_000.0;
        classResults.forEach(a -> a.executionMs = elapsed);
        requestResults.forEach(a -> a.executionMs = elapsed);
        allocations.saveAll(classResults);
        spaceAssignments.saveAll(requestResults);

        Map<Long, SpaceAssignment> resultByRequest = new HashMap<>();
        requestResults.forEach(a -> resultByRequest.put(a.requestId, a));
        for (SpaceRequest request : dayRequests) {
            SpaceAssignment result = resultByRequest.get(request.id);
            if (result == null) continue;
            if ("STUDY".equals(request.type))
                request.status = "Allocated".equals(result.status) ? "ALLOCATED" : "WAITLISTED";
            else if (isActiveRequest(request))
                request.status = "Allocated".equals(result.status) ? "BOOKED" : "CONFIRMED";
        }
        requests.saveAll(dayRequests);

        int total = classResults.size() + requestResults.size();
        long allocated = classResults.stream().filter(a -> "Allocated".equals(a.status)).count()
                + requestResults.stream().filter(a -> "Allocated".equals(a.status)).count();
        long invalid = classResults.stream().filter(a -> "Invalid".equals(a.status)).count();
        long firstFit = 0;
        for (TimePeriod slot : slots) {
            List<ClassroomAllocator.Session> input = day.stream().filter(c -> sameTime(c, slot))
                    .map(c -> new ClassroomAllocator.Session(c.classId, c.studentCount,
                            minute(c.startTime), minute(c.endTime))).toList();
            firstFit += ClassroomAllocator.firstFit(input, allRooms.stream()
                    .map(r -> new ClassroomAllocator.Room(r.name, r.capacity, r.available)).toList()).allocatedCount();
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("date", date);
        response.put("total", total);
        response.put("allocated", allocated);
        response.put("unallocated", total - allocated - invalid);
        response.put("invalid", invalid);
        response.put("conflictPairs", countConflicts(day));
        response.put("executionMs", elapsed);
        response.put("firstFitAllocated", firstFit);
        response.put("classBaseline", matchedClasses);
        response.put("results", classResults);
        response.put("spaceResults", requestResults);
        return response;
    }

    public Map<String, Object> previewEvent(SpaceRequest candidate) {
        validateRequest(candidate);
        LocalTime start = requestStart(candidate), end = requestEnd(candidate);
        List<Room> allRooms = sortedRooms();
        List<TimePeriod> slots = activePeriods();
        List<ClassSession> day = sessions.findByDate(candidate.date);
        Map<Long, SegmentTree> baselineTrees = newTrees(allRooms);
        int classesBefore = matchDayClasses(day, slots, allRooms, baselineTrees, null, null, null);

        if (candidate.floorWide) {
            int floor = candidate.preferredFloor;
            Map<Long, SegmentTree> afterTrees = newTrees(allRooms);
            int classesAfter = matchDayClasses(day, slots, allRooms, afterTrees, floor, start, end);
            List<Room> floorRooms = allRooms.stream().filter(r -> r.available && r.floor == floor).toList();
            long seats = floorRooms.stream().mapToLong(r -> r.capacity).sum();
            boolean free = !floorRooms.isEmpty() && floorRooms.stream()
                    .allMatch(r -> baselineTrees.get(r.id).isFree(minute(start), minute(end)));
            return Map.of("feasible", classesAfter == classesBefore && seats >= candidate.headcount && free,
                    "classesBefore", classesBefore, "classesAfter", classesAfter,
                    "floorAvailableSeats", seats,
                    "alternatives", alternativeFloors(candidate, allRooms, day, slots, classesBefore, start, end));
        }

        List<Map<String, Object>> alternatives = allRooms.stream()
                .filter(r -> r.available && r.capacity >= candidate.headcount)
                .filter(r -> baselineTrees.get(r.id).isFree(minute(start), minute(end)))
                .sorted(Comparator.comparing((Room r) -> candidate.preferredRoom != null && !candidate.preferredRoom.equals(r.name))
                        .thenComparing(r -> candidate.preferredFloor != null && r.floor != candidate.preferredFloor)
                        .thenComparingInt(r -> r.capacity).thenComparing(r -> r.name))
                .limit(5)
                .map(r -> Map.<String, Object>of("room", r.name, "floor", r.floor,
                        "capacity", r.capacity, "time", timeLabel(start, end)))
                .toList();
        boolean hasCapacity = allRooms.stream().anyMatch(r -> r.available && r.capacity >= candidate.headcount);
        return Map.of("feasible", !alternatives.isEmpty(), "classesProtected", classesBefore,
                "capacityFit", hasCapacity, "alternatives", alternatives);
    }

    private List<Map<String, Object>> alternativeFloors(SpaceRequest candidate, List<Room> rooms,
                                                         List<ClassSession> classes, List<TimePeriod> slots,
                                                         int baseline, LocalTime start, LocalTime end) {
        return java.util.stream.IntStream.rangeClosed(1, 7).filter(f -> f != candidate.preferredFloor)
                .mapToObj(f -> {
                    Map<Long, SegmentTree> trees = newTrees(rooms);
                    int preserved = matchDayClasses(classes, slots, rooms, trees, f, start, end);
                    long seats = rooms.stream().filter(r -> r.floor == f && r.available).mapToLong(r -> r.capacity).sum();
                    boolean feasible = preserved == baseline && seats >= candidate.headcount;
                    return Map.<String, Object>of("floor", f, "classesPreserved", preserved == baseline,
                            "availableSeats", seats, "feasible", feasible, "time", timeLabel(start, end));
                }).filter(x -> Boolean.TRUE.equals(x.get("feasible"))).toList();
    }

    private int matchDayClasses(List<ClassSession> classes, List<TimePeriod> slots, List<Room> rooms,
                                Map<Long, SegmentTree> trees, Integer blockedFloor,
                                LocalTime blockStart, LocalTime blockEnd) {
        int assignedCount = 0;
        for (TimePeriod slot : slots) {
            List<ClassSession> slotClasses = classes.stream().filter(c -> sameTime(c, slot)).toList();
            boolean blockApplies = blockedFloor != null && overlaps(slot.startTime, slot.endTime, blockStart, blockEnd);
            Set<Integer> blocked = blockApplies ? Set.of(blockedFloor) : Set.of();
            MaximumBipartiteMatcher.Result result = matchClasses(slotClasses, rooms, trees, blocked);
            assignedCount += result.cardinality();
            int[] roomByClass = result.rightByLeft();
            for (int i = 0; i < roomByClass.length; i++) {
                if (roomByClass[i] >= 0) {
                    ClassSession c = slotClasses.get(i);
                    Room room = rooms.get(roomByClass[i]);
                    trees.get(room.id).occupy(minute(c.startTime), minute(c.endTime));
                }
            }
        }
        return assignedCount;
    }

    private SpaceAssignment assignFloorEvent(SpaceRequest event, List<Room> rooms, Map<Long, SegmentTree> trees) {
        LocalTime start = requestStart(event), end = requestEnd(event);
        int floor = event.preferredFloor == null ? 0 : event.preferredFloor;
        List<Room> floorRooms = rooms.stream().filter(r -> r.available && r.floor == floor).toList();
        long capacity = floorRooms.stream().mapToLong(r -> r.capacity).sum();
        boolean free = !floorRooms.isEmpty() && floorRooms.stream()
                .allMatch(r -> trees.get(r.id).isFree(minute(start), minute(end)));
        SpaceAssignment result = toAssignment(event, start, end);
        if (free && capacity >= event.headcount) {
            floorRooms.forEach(r -> trees.get(r.id).occupy(minute(start), minute(end)));
            result.status = "Allocated";
            result.floor = floor;
        } else {
            result.status = "Unallocated";
            result.reason = capacity < event.headcount
                    ? "Available seating on this floor is below the event headcount."
                    : "This floor is occupied during part of the requested time.";
        }
        return result;
    }

    private SpaceAssignment assignSmallestRoom(SpaceRequest request, List<Room> rooms, Map<Long, SegmentTree> trees) {
        LocalTime start = requestStart(request), end = requestEnd(request);
        SpaceAssignment result = toAssignment(request, start, end);
        Room selected = rooms.stream()
                .filter(r -> r.available && r.capacity >= request.headcount)
                .filter(r -> trees.get(r.id).isFree(minute(start), minute(end)))
                .sorted(Comparator.comparing((Room r) -> request.preferredRoom != null && !request.preferredRoom.equals(r.name))
                        .thenComparing(r -> request.preferredFloor != null && r.floor != request.preferredFloor)
                        .thenComparingInt(r -> r.capacity).thenComparing(r -> r.name))
                .findFirst().orElse(null);
        if (selected == null) {
            result.status = "Unallocated";
            boolean canFit = rooms.stream().anyMatch(r -> r.available && r.capacity >= request.headcount);
            result.reason = canFit
                    ? "All suitable classrooms are occupied during this time. Try another time."
                    : "No available classroom can hold " + request.headcount + " students.";
            return result;
        }
        trees.get(selected.id).occupy(minute(start), minute(end));
        result.status = "Allocated";
        result.assignedRoom = selected.name;
        result.floor = selected.floor;
        if (request.preferredRoom != null && !request.preferredRoom.equals(selected.name))
            result.reason = "Room " + request.preferredRoom + " was unavailable; assigned " + selected.name + " instead.";
        else if (request.preferredFloor != null && selected.floor != request.preferredFloor)
            result.reason = "No free suitable room on preferred floor " + request.preferredFloor
                    + "; assigned the smallest free suitable room on floor " + selected.floor + " instead.";
        return result;
    }

    public void validateRequest(SpaceRequest request) {
        if (request.type == null || !List.of("EVENT", "STUDY").contains(request.type))
            throw new IllegalArgumentException("Request type must be EVENT or STUDY.");
        if (request.title == null || request.title.isBlank()) throw new IllegalArgumentException("Enter a title or group name.");
        if (request.headcount <= 0) throw new IllegalArgumentException("Headcount must be positive.");
        if (request.date == null) throw new IllegalArgumentException("Choose a date.");
        LocalTime start = requestStart(request), end = requestEnd(request);
        if (start == null || end == null || !start.isBefore(end) || start.getSecond() != 0 || end.getSecond() != 0
                || start.getNano() != 0 || end.getNano() != 0)
            throw new IllegalArgumentException("Enter valid start and end times with the start before the end.");
        if (request.preferredFloor != null && (request.preferredFloor < 1 || request.preferredFloor > 7))
            throw new IllegalArgumentException("Floor must be between 1 and 7.");
        if (request.floorWide && (!"EVENT".equals(request.type) || request.preferredFloor == null))
            throw new IllegalArgumentException("Choose a floor for a floor-wide event.");
    }

    private LocalTime requestStart(SpaceRequest r) {
        if (r.startTime != null) return r.startTime;
        if (r.periodId == null || r.periodId == 0) return null;
        return periods.findById(r.periodId).map(p -> p.startTime).orElse(null);
    }

    private LocalTime requestEnd(SpaceRequest r) {
        if (r.endTime != null) return r.endTime;
        if (r.periodId == null || r.periodId == 0) return null;
        return periods.findById(r.periodId).map(p -> p.endTime).orElse(null);
    }

    private SpaceAssignment toAssignment(SpaceRequest r, LocalTime start, LocalTime end) {
        SpaceAssignment a = new SpaceAssignment();
        a.requestId = r.id;
        a.type = r.type;
        a.title = r.title;
        a.requesterName = r.requesterName;
        a.headcount = r.headcount;
        a.date = r.date;
        a.periodId = r.periodId == null || r.periodId == 0 ? 0L : r.periodId;
        a.timeLabel = timeLabel(start, end);
        return a;
    }

    private Allocation classAllocation(ClassSession c, TimePeriod slot) {
        Allocation a = new Allocation();
        a.sessionId = c.id;
        a.date = c.date;
        a.classId = c.classId;
        a.school = c.school;
        a.program = c.program;
        a.division = c.division;
        a.studentCount = c.studentCount;
        a.timeLabel = timeLabel(c.startTime, c.endTime);
        if (slot != null) {
            a.periodId = slot.id;
            a.periodName = slot.name;
        }
        a.demandType = "CLASS";
        return a;
    }

    private MaximumBipartiteMatcher.Result matchClasses(List<ClassSession> classes, List<Room> rooms,
                                                         Map<Long, SegmentTree> trees, Set<Integer> excludedFloors) {
        boolean[][] edges = new boolean[classes.size()][rooms.size()];
        for (int i = 0; i < classes.size(); i++) {
            ClassSession c = classes.get(i);
            for (int j = 0; j < rooms.size(); j++) {
                Room r = rooms.get(j);
                edges[i][j] = r.available && !excludedFloors.contains(r.floor) && r.capacity >= c.studentCount
                        && trees.get(r.id).isFree(minute(c.startTime), minute(c.endTime));
            }
        }
        return MaximumBipartiteMatcher.match(edges);
    }

    private List<Room> sortedRooms() {
        return rooms.findAll().stream().sorted(Comparator.comparingInt((Room r) -> r.capacity).thenComparing(r -> r.name)).toList();
    }

    private List<TimePeriod> activePeriods() {
        return periods.findAll().stream().filter(p -> p.active).sorted(Comparator.comparing(p -> p.startTime)).toList();
    }

    private Map<Long, SegmentTree> newTrees(List<Room> rooms) {
        Map<Long, SegmentTree> trees = new HashMap<>();
        rooms.forEach(r -> trees.put(r.id, new SegmentTree()));
        return trees;
    }

    private boolean sameTime(ClassSession c, TimePeriod p) {
        return c.startTime.equals(p.startTime) && c.endTime.equals(p.endTime);
    }

    private boolean isActiveRequest(SpaceRequest r) {
        return List.of("CONFIRMED", "BOOKED").contains(r.status);
    }

    private int minute(LocalTime time) { return time.getHour() * 60 + time.getMinute(); }
    private String timeLabel(LocalTime start, LocalTime end) { return start + "–" + end; }

    private boolean overlaps(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    private long countConflicts(List<ClassSession> sessions) {
        long count = 0;
        for (int i = 0; i < sessions.size(); i++) for (int j = i + 1; j < sessions.size(); j++) {
            ClassSession a = sessions.get(i), b = sessions.get(j);
            if (a.date.equals(b.date) && overlaps(a.startTime, a.endTime, b.startTime, b.endTime)) count++;
        }
        return count;
    }
}
