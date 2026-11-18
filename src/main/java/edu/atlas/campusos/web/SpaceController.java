package edu.atlas.campusos.web;

import edu.atlas.campusos.model.*;import edu.atlas.campusos.repo.*;import edu.atlas.campusos.service.AllocationService;
import org.springframework.data.domain.Sort;import org.springframework.web.bind.annotation.*;import java.time.*;import java.util.*;

@RestController @RequestMapping("/api")
public class SpaceController {
    private final TimePeriodRepository periods;private final SpaceRequestRepository requests;private final SpaceAssignmentRepository assignments;private final AllocationService allocator;private final SessionRepository sessions;
    public SpaceController(TimePeriodRepository p,SpaceRequestRepository r,SpaceAssignmentRepository a,AllocationService allocator,SessionRepository s){periods=p;requests=r;assignments=a;this.allocator=allocator;sessions=s;}
    @GetMapping("/periods") public List<TimePeriod> periods(){return periods.findAll(Sort.by("startTime"));}
    @PostMapping("/periods") public TimePeriod addPeriod(@RequestBody TimePeriod p){validatePeriod(p,null);p.id=null;return periods.save(p);}
    @PutMapping("/periods/{id}") public TimePeriod editPeriod(@PathVariable long id,@RequestBody TimePeriod p){TimePeriod old=periods.findById(id).orElseThrow();validatePeriod(p,id);old.name=p.name;old.startTime=p.startTime;old.endTime=p.endTime;old.active=p.active;return periods.save(old);}
    @DeleteMapping("/periods/{id}") public Map<String,Object> deletePeriod(@PathVariable long id){TimePeriod p=periods.findById(id).orElseThrow();if(requests.findAll().stream().anyMatch(r->Objects.equals(r.periodId,id))||sessions.findAll().stream().anyMatch(s->s.startTime.equals(p.startTime)&&s.endTime.equals(p.endTime)))throw new IllegalStateException("This period is used by timetable or booking records.");periods.delete(p);return Map.of("deleted",true);}
    private void validatePeriod(TimePeriod p,Long exclude){if(p.name==null||p.name.isBlank()||p.startTime==null||p.endTime==null||!p.startTime.isBefore(p.endTime)||p.startTime.getSecond()!=0||p.endTime.getSecond()!=0)throw new IllegalArgumentException("Enter a name and valid minute-aligned start/end times.");for(TimePeriod x:periods.findAll())if(!Objects.equals(x.id,exclude)&&x.active&&p.active&&p.startTime.isBefore(x.endTime)&&x.startTime.isBefore(p.endTime))throw new IllegalArgumentException("Periods cannot overlap; matching requires one room per non-overlapping period.");}

    @GetMapping("/space-requests") public List<SpaceRequest> list(@RequestParam(required=false)LocalDate date){return requests.findAll(Sort.by("date","createdAt")).stream().filter(r->date==null||date.equals(r.date)).toList();}
    @PostMapping("/space-requests") public SpaceRequest addStudyRequest(@RequestBody SpaceRequest r){r.type="STUDY";r.floorWide=false;r.status="PENDING";useDirectTimes(r);allocator.validateRequest(r);r.id=null;return requests.save(r);}
    @PostMapping("/events/preview") public Map<String,Object> previewEvent(@RequestBody SpaceRequest r){r.type="EVENT";r.status="CONFIRMED";useDirectTimes(r);return allocator.previewEvent(r);}
    @PostMapping("/events") public SpaceRequest addEvent(@RequestBody SpaceRequest r){r.type="EVENT";r.status="CONFIRMED";useDirectTimes(r);allocator.validateRequest(r);Map<String,Object> preview=allocator.previewEvent(r);if(!Boolean.TRUE.equals(preview.get("feasible")))throw new IllegalStateException("The event would conflict with classes or lacks capacity. Review suggested alternatives before confirming.");r.id=null;return requests.save(r);}
    private void useDirectTimes(SpaceRequest r){if(r.startTime!=null&&r.endTime!=null)r.periodId=0L;}
    @DeleteMapping("/space-requests/{id}") public Map<String,Object> cancel(@PathVariable long id){SpaceRequest r=requests.findById(id).orElseThrow();assignments.deleteAll(assignments.findAll().stream().filter(a->Objects.equals(a.requestId,id)).toList());r.status="CANCELLED";requests.save(r);return Map.of("cancelled",true);}
}
