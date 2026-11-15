package edu.atlas.campusos.model;

import jakarta.persistence.*;
import java.time.*;

@Entity @Table(name="class_sessions", uniqueConstraints=@UniqueConstraint(columnNames={"classId","date","startTime","division"}))
public class ClassSession {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public String classId;
    @Column(nullable=false) public String school;
    @Column(nullable=false) public String program;
    @Column(nullable=false) public int academicYear;
    @Column(nullable=false) public String division;
    @Column(nullable=false) public int studentCount;
    @Column(nullable=false) public LocalDate date;
    @Column(nullable=false) public LocalTime startTime;
    @Column(nullable=false) public LocalTime endTime;
    public ClassSession() {}
}
