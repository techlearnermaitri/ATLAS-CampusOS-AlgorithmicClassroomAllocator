package edu.atlas.campusos.model;

import jakarta.persistence.*;import java.time.LocalTime;

@Entity @Table(name="time_periods")
public class TimePeriod {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false,unique=true) public String name;
    @Column(nullable=false) public LocalTime startTime;
    @Column(nullable=false) public LocalTime endTime;
    @Column(nullable=false) public boolean active=true;
    public TimePeriod(){}
}
