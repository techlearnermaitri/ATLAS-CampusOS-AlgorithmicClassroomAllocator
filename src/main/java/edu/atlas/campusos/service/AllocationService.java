package edu.atlas.campusos.service;

import edu.atlas.campusos.model.*;import edu.atlas.campusos.repo.*;
import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
import java.time.*;import java.util.*;

@Service
public class AllocationService {
    private final RoomRepository rooms;private final SessionRepository sessions;private final AllocationRepository allocations;
    public AllocationService(RoomRepository r,SessionRepository s,AllocationRepository a){rooms=r;sessions=s;allocations=a;}

    @Transactional public Map<String,Object> run(LocalDate date,boolean replace){
        long started=System.nanoTime();List<ClassSession> classes=sessions.findByDate(date);List<Allocation> prior=allocations.findBySessionIdIn(classes.stream().map(x->x.id).toList());
        if(!prior.isEmpty()&&!replace)throw new IllegalStateException("Results already exist for this date. Confirm replacement to run again.");if(replace&&!prior.isEmpty())allocations.deleteAll(prior);
        List<Room> available=rooms.findAll().stream().filter(r->r.available).sorted(Comparator.comparingInt(r->r.capacity)).toList();
        List<ClassroomAllocator.Session> problem=classes.stream().map(s->new ClassroomAllocator.Session(s.classId,s.studentCount,minute(s.startTime),minute(s.endTime))).toList();
        List<ClassroomAllocator.Room> problemRooms=available.stream().map(r->new ClassroomAllocator.Room(r.name,r.capacity,true)).toList();
        ClassroomAllocator.Result result=ClassroomAllocator.dsatur(problem,problemRooms);int[] assignment=result.roomBySession();List<Allocation> saved=new ArrayList<>();
        for(int i=0;i<classes.size();i++){ClassSession c=classes.get(i);Allocation a=new Allocation();a.sessionId=c.id;a.classId=c.classId;a.school=c.school;a.program=c.program;a.division=c.division;a.studentCount=c.studentCount;a.timeLabel=c.startTime+"–"+c.endTime;int roomIndex=assignment[i];
            if(roomIndex<0){a.status="Unallocated";boolean enough=available.stream().anyMatch(r->r.capacity>=c.studentCount);a.reason=enough?"No available room exists during the selected interval.":"No room has sufficient capacity.";}else{Room room=available.get(roomIndex);a.status="Allocated";a.assignedRoom=room.name;a.floor=room.floor;}saved.add(a);}
        List<AllocationAlgorithms.FlowClass> fc=classes.stream().map(c->new AllocationAlgorithms.FlowClass(c.studentCount,c.startTime.toString(),c.endTime.toString())).toList();List<AllocationAlgorithms.FlowRoom> fr=available.stream().map(r->new AllocationAlgorithms.FlowRoom(r.name,r.capacity,true)).toList();
        var flow=AllocationAlgorithms.flowEstimate(fc,fr);long firstFit=ClassroomAllocator.firstFit(problem,problemRooms).allocatedCount();long allocated=saved.stream().filter(a->"Allocated".equals(a.status)).count();double elapsed=(System.nanoTime()-started)/1_000_000.0;saved.forEach(a->a.executionMs=elapsed);allocations.saveAll(saved);
        return Map.of("date",date,"total",classes.size(),"allocated",allocated,"unallocated",classes.size()-allocated,"conflictPairs",result.conflictPairs(),"executionMs",elapsed,"flowEstimate",flow,"firstFitAllocated",firstFit,"results",saved);
    }
    private int minute(LocalTime t){return t.getHour()*60+t.getMinute();}
}
