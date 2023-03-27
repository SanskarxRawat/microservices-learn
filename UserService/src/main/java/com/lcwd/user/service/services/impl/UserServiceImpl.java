package com.lcwd.user.service.services.impl;

import com.lcwd.user.service.entities.Hotel;
import com.lcwd.user.service.entities.Rating;
import com.lcwd.user.service.entities.User;
import com.lcwd.user.service.exceptions.ResourceNotFoundException;
import com.lcwd.user.service.external.services.HotelService;
import com.lcwd.user.service.repositories.UserRepository;
import com.lcwd.user.service.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;


@Service
public class UserServiceImpl implements UserService {

    private Logger logger = LoggerFactory.getLogger (UserServiceImpl.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HotelService hotelService;

    @Autowired
    RedisTemplate redisTemplate;

    private static final String KEY="USER";
    @Override
    public User saveUser(User user) {
        String randomUserId= UUID.randomUUID().toString();
        user.setUserId(randomUserId);
        try {
            redisTemplate.opsForHash ().put (KEY,randomUserId,user);

        }catch (Exception e){
            e.printStackTrace ();
        }

        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
//        List<User> allUser=redisTemplate.opsForHash ().values (KEY);
        return userRepository.findAll();
//        return allUser;
    }

    @Override
    public User getUser(String userId) {
        User user =userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("User with given id not found: "+userId));

        Rating[] ratingOfUser = restTemplate.getForObject("http://RATING-SERVICE/ratings/users/"+user.getUserId (),Rating[].class);
        logger.info("{}",ratingOfUser);

        List<Rating> ratings=Arrays.stream (ratingOfUser).toList ();

        List<Rating> ratingList=ratings.stream ().map(rating -> {
            //api call to hotel service to get the hotel
            //http://localhost:9193/hotels
//            ResponseEntity<Hotel> forEntity= restTemplate.getForEntity ("http://HOTEL-SERVICE/hotels/"+rating.getHotelId (), Hotel.class);
            //set the hotel to rating
            Hotel hotel=hotelService.getHotel(rating.getHotelId());
//            logger.info("response status code",forEntity.getStatusCode ());
            rating.setHotel(hotel);
            //return the rating
            return rating;

        }).collect (Collectors.toList ());

        user.setRatings(ratingList);
        return user;
    }
}
