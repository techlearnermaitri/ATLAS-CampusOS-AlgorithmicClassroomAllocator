package edu.atlas.campusos.service;

import java.util.*;

public final class Curriculum {
    private Curriculum() {}
    public record School(String code,String name,int weight,int duration,List<String> programs) {}
    public static final List<School> SCHOOLS=List.of(
      new School("UGDX","Technology",14,4,List.of("B.Tech Computer Science — AI and Data Science","B.Tech Computer Science — AI and Machine Learning","B.Tech Computer Science — AI and Robotics","B.Tech Information Technology — AI and Cybersecurity","B.Tech Quantum Technology","B.Tech Semiconductor Technology")),
      new School("ISME","Management and Business",65,4,List.of("BBA","BBA Honours — Marketing","BBA Honours — Finance","BBA Honours — Business Analytics","BBA Honours — Digital Branding and Advertising","BBA Honours — AI and Emerging Technologies","BBA Honours — Business Psychology","BBA Honours — International Accounting and Finance","B.Sc. (Hons.) Finance","B.Sc. (Hons.) Economics and Data Analytics")),
      new School("ISDI","Design",55,4,List.of("B.Des Product Design","B.Des Fashion Design and Technology","B.Des Fashion Communication and Styling","B.Des Fashion Brand Management","B.Des Luxury Brand Strategy","B.Des Accessory Design and Craft","B.Des Communication Design and New Media","B.Des Animation, VFX and Gaming","B.Des Interior Design and Built Environment","B.Des Strategic Design and Innovation Management","B.Des Service and Experience Design","B.Des Health and Care Design","B.Tech Design Engineering","B.Tech AI Engineering and Intelligent Products")),
      new School("LAW","Law",2,5,List.of("Integrated BBA LL.B."))
    );
    public static int duration(String school){return SCHOOLS.stream().filter(s->s.code().equalsIgnoreCase(school)).findFirst().map(School::duration).orElse(0);}
    public static boolean hasProgram(String school,String program){return SCHOOLS.stream().filter(s->s.code().equalsIgnoreCase(school)).anyMatch(s->s.programs().contains(program));}
    public static double proportion(int weight){return (double)weight/136;}
}
