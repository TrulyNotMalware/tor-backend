package com.hackerton.tor.torback.login;

import com.hackerton.tor.torback.entity.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import org.springframework.hateoas.*;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.*;

import java.util.HashMap;

@Slf4j
@AllArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/login")
public class LoginController {

    private LoginServices services;

    @PostMapping(value = "/doLogin",produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<JSONObject> doLogin(
            @RequestBody HashMap<String, String> params
    ){
        /**
         * Try Login.
         * Required : id (userId), pwd (userPwd)
         * Return : JSON, { "success" : boolean }
         **/
        log.debug("doLogin, param {}, {}",params.get("userId"),params.get("password"));
        return this.services.doLogin(params.get("userId"),params.get("password"));
    }

    @GetMapping(value = "/checkDuplicated/{userId}",produces = MediaTypes.HAL_JSON_VALUE)
    public Mono<EntityModel<User>> getIsUserDuplicated(
            @PathVariable String userId
    ){
        LoginController controller = methodOn(LoginController.class);
        Mono<Link> selfLink = linkTo(controller.getIsUserDuplicated(userId))
                .withSelfRel()
                .toMono();
        return Mono.zip(this.services.isDuplicatedUser(userId),selfLink)
                .map(objects -> EntityModel.of(objects.getT1(),objects.getT2()));
    }

    @PostMapping(value = "/signUp", produces = MediaTypes.HAL_JSON_VALUE )
    public Mono<EntityModel<User>> createNewUser(
        @RequestBody HashMap<String, String> params
    ){
        /**
         * Register New users.
         * Required : id (userId), pwd (userPwd)
         * Return : JSON, { user, links }
         **/
        Mono<Links> allLinks;
        LoginController controller = methodOn(LoginController.class);
        // SelfLinks inserts.
        Mono<Link> selfLink = linkTo(controller.createNewUser(params))
                .withSelfRel()
                .toMono();
        Mono<Link> aggregateLink = linkTo(controller.doLogin(params))
                .withRel("login")
                .map(link -> link.withTitle("Login_methods")).toMono();

        //Link zip
        allLinks = Mono.zip(selfLink,aggregateLink)
                .map(links -> Links.of(links.getT1(),links.getT2()));

        return this.services.insertUser(params.get("userId"),params.get("userName")
                ,params.get("password"), params.get("email"))
                .zipWith(allLinks)
                .map(objects -> EntityModel.of(objects.getT1(),objects.getT2()));
    }
}
