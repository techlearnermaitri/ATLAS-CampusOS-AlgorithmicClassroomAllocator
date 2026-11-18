package edu.atlas.campusos.model;

import jakarta.persistence.*;import java.time.LocalDate;

@Entity @Table(name="space_assignments")
public class SpaceAssignment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public Long requestId;
    @Column(nullable=false) public String type;
    @Column(nullable=false) public String title;
    public String requesterName;
    @Column(nullable=false) public int headcount;
    @Column(nullable=false) public LocalDate date;
    @Column(nullable=false) public Long periodId;
    public String timeLabel,assignedRoom,reason,status;
    public int floor;
    public double executionMs;
    public SpaceAssignment(){}
}
