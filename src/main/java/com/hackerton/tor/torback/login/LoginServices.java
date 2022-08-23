package com.hackerton.tor.torback.login;

import com.hackerton.tor.torback.entity.User;
import com.hackerton.tor.torback.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;

@Slf4j
@AllArgsConstructor
@Service
public class LoginServices {

    //User Repository
    private UserRepository userRepository;

    public Mono<JSONObject> doLogin(@NotNull String id, @NotNull String pwd){ //id&pwd can't be null
        return this.userRepository.findById(id)
                .doOnError(error -> {
                    log.trace(error.getMessage());
                })
                .map(user -> {
                    JSONObject result = new JSONObject();
                    /**
                     * [FIXME] Login Logic need to change.
                     **/
                    if(user.getUserId().equals(id) && user.getPassword().equals(pwd)){
                        result.put("success",true);
                        result.put("user",user);
                    }
                    else{
                        result.put("success",false);
                    }
                    return result;
                });
    }

    public Mono<User> insertUser(@NotNull String userId, @NotNull String userName, @NotNull String password, String email){
        return this.userRepository.insertUser(userId,userName,password,email)
                .doOnError(error -> log.trace(error.getMessage())).log("User inserted")
                .then(this.userRepository.findById(userId))
                .log("User insert");

    }

    public Mono<User> isDuplicatedUser(@NotNull String userId){
        return this.userRepository.selectUserById(userId)
                .doOnError(error -> log.trace(error.getMessage()))
                .switchIfEmpty(Mono.defer(() -> Mono.empty()));
    }

}
