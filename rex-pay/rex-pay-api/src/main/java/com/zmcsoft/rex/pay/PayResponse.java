package com.zmcsoft.rex.pay;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayResponse {
    private boolean success;

    private String htmlForm;

    private String message;

}
