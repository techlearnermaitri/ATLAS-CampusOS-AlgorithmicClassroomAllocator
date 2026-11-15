package edu.atlas.campusos.model;

import jakarta.persistence.*;

@Entity @Table(name="allocations")
public class Allocation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public Long sessionId;
    public String classId, school, program, division, assignedRoom, reason, status, timeLabel;
    public int studentCount, floor;
    public double executionMs;
    public Allocation() {}
}
