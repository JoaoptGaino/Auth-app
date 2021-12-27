package com.joaoptgaino.authapp.registration;

import com.joaoptgaino.authapp.appuser.AppUser;
import com.joaoptgaino.authapp.appuser.AppUserRole;
import com.joaoptgaino.authapp.appuser.AppUserService;
import com.joaoptgaino.authapp.email.EmailSender;
import com.joaoptgaino.authapp.email.EmailService;
import com.joaoptgaino.authapp.registration.token.ConfirmationToken;
import com.joaoptgaino.authapp.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final AppUserService appUserService;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailValidator emailValidator;
    private final EmailService emailService;
    private final EmailSender emailSender;

    //TODO: Create user DTO
    public String register(RegistrationRequest request) {
        boolean isValidEmail = emailValidator.test(request.getEmail());

        if (!isValidEmail) {
            throw new IllegalStateException("Invalid e-mail");
        }

        String token = appUserService.signUpUser(new AppUser(
                        request.getFirstName(),
                        request.getLastName(),
                        request.getEmail(),
                        request.getPassword(),
                        AppUserRole.USER
                )
        );

        String confirmationLink = "http://localhost:8080/api/v1/registration/confirm?token=" + token;

        emailSender.send(request.getEmail(), emailService.buildEmail(request.getFirstName(), confirmationLink));

        return token;
    }

    @Transactional
    public String confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.getToken(token).orElseThrow(() -> new IllegalStateException("Token not found."));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("E-mail already confirmed.");
        }
        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Token expired.");
        }

        confirmationTokenService.setConfirmedAt(token);

        appUserService.enableAppUser(confirmationToken.getAppUser().getEmail());

        return "E-mail confirmed successfully.";
    }
}
