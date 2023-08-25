package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

//WORK
@RestController
public class UserController {

    // TODO: wire in the user repository (~ 1 line)
    @Autowired UserRepository UserRepo;
    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {
        // TODO: Create an user entity with information given in the payload, store it in the database
        //       and return the id of the user in 200 OK response

        // Create a new user entity based on the payload data.
        User newUser = new User();
        newUser.setName(payload.getName());
        newUser.setEmail(payload.getEmail());

        // Save the new user in the database.
        UserRepo.save(newUser);

        // Return a response with the newly created user's ID in the body.
        return ResponseEntity.status(HttpStatus.OK).body(newUser.getId());
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {
        // TODO: Return 200 OK if a user with the given ID exists, and the deletion is successful
        //       Return 400 Bad Request if a user with the ID does not exist
        //       The response body could be anything you consider appropriate

        // Check if the user with the given ID exists.
        Optional<User> deleteUser = UserRepo.findById(userId);
        if(deleteUser.isEmpty()){
            // Return a response with a 400 Bad Request status and a message.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User: "+ userId+ "does NOT exist");
        }

        // Delete the user with the given ID.
        UserRepo.deleteById(userId);

        // Return a response with a 200 OK status and a success message.
        return ResponseEntity.status(HttpStatus.OK).body("User: "+ userId+ " has been DELETED");
    }
}
