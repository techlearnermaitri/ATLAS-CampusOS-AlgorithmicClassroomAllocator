package edu.atlas.campusos.web;
import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestControllerAdvice public class ApiErrors {
 @ExceptionHandler({IllegalArgumentException.class,IllegalStateException.class,NoSuchElementException.class}) public ResponseEntity<Map<String,String>> client(RuntimeException e){return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()==null?"Request could not be completed.":e.getMessage()));}
}
