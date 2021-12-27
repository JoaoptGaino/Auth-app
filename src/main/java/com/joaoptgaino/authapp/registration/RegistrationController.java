package com.joaoptgaino.authapp.registration;

import com.joaoptgaino.authapp.appuser.CreateAppUserDto;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "api/v1/registration")
@AllArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;

    @PostMapping
    public String register(@Valid @RequestBody CreateAppUserDto request){
        return registrationService.register(request);
    }

    @GetMapping(path="confirm")
    public String confirm(@RequestParam("token") String token){
        return registrationService.confirmToken(token);
    }
}
