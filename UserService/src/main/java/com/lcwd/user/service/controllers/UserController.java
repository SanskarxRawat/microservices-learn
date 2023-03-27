package com.lcwd.user.service.controllers;


import com.lcwd.user.service.entities.User;
import com.lcwd.user.service.services.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.util.internal.logging.InternalLoggerFactory;
import lombok.Builder;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;
    //private Logger logger= (Logger) LoggerFactory.getLogger (UserController.class);


    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user){
        User users1=userService.saveUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(users1);
    }


    @GetMapping("/{userId}")
    @CircuitBreaker (name="ratingHotelBreaker",fallbackMethod = "ratingHotelFallback")
    public ResponseEntity<User> getUser(@PathVariable String userId){

        User user=userService.getUser(userId);
        return ResponseEntity.ok(user);
    }

    //creating fallback for circuit breaker

    public ResponseEntity<User> ratingHotelFallback(String userId,Exception e){
       // logger.info("Fallback is executed because service is down");
        User user=User.builder ()
                .email ("dummy@gmail.com")
                .name ("Dummy")
                .about ("This service is created because service is down")
                .userId ("1234")
                .build ();
        return new ResponseEntity<> (user,HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(){
        List<User> allUser=userService.getAllUsers();
        return ResponseEntity.ok(allUser);
    }
}
