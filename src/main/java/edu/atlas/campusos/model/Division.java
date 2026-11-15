package edu.atlas.campusos.model;

import jakarta.persistence.*;

@Entity @Table(name="divisions", uniqueConstraints=@UniqueConstraint(columnNames={"school","program","academicYear","name"}))
public class Division {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    public String school, program, name;
    public int academicYear;
    public Integer actualStudents;
    public Division() {}
}
