package org.example.accountservice.entity.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
public class ConfirmResetRequest {
    @Email
    private String email;

    @Length(min = 6, max = 6)
    private String code;
}
