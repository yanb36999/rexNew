package com.zmcsoft.rex.pay;


import lombok.*;

import java.util.Map;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayRequest {

    private Map<String, String> parameters;

    public String getParameter(String key){
        return parameters.getOrDefault(key,"");
    }

    public String getParameter(String key,String defaultValue){
        return parameters.getOrDefault(key,defaultValue);
    }

}
