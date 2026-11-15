package edu.atlas.campusos.model;

import jakarta.persistence.*;

@Entity @Table(name="classrooms")
public class Room {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false, unique=true) public String name;
    @Column(nullable=false) public int floor;
    @Column(nullable=false) public int capacity;
    @Column(nullable=false) public boolean available=true;
    public Room() {}
    public Room(String name,int floor,int capacity,boolean available){this.name=name;this.floor=floor;this.capacity=capacity;this.available=available;}
}
