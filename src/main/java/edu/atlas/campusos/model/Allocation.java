package edu.atlas.campusos.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity @Table(name="allocations")
public class Allocation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public Long sessionId;
    public LocalDate date;
    public Long periodId;
    public String periodName;
    public String demandType="CLASS";
    public String classId, school, program, division, assignedRoom, reason, status, timeLabel;
    public int studentCount, floor;
    public double executionMs;
    public Allocation() {}
}
