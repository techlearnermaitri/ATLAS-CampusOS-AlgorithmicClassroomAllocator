package edu.atlas.campusos.model;

import jakarta.persistence.*;import java.time.*;

@Entity @Table(name="space_requests")
public class SpaceRequest {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public String type;
    @Column(nullable=false) public String title;
    public String requesterName;
    @Column(nullable=false) public int headcount;
    @Column(nullable=false) public LocalDate date;
    @Column(nullable=false) public Long periodId=0L;
    public LocalTime startTime;
    public LocalTime endTime;
    @Column(nullable=false) public boolean floorWide;
    public Integer preferredFloor;
    public String preferredRoom;
    @Column(nullable=false) public String status="PENDING";
    @Column(nullable=false) public LocalDateTime createdAt=LocalDateTime.now();
    public SpaceRequest(){}
}
