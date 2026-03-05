package com.example.Module1.Practice.Bakery.Concrete_Syrups;

import com.example.Module1.Practice.Bakery.Syrup;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "syrup.type",havingValue = "chocolate")
public class ChocolateSyrup implements Syrup {
    @Override
    public String getSyrupType() {
        return "Chocolate";
    }
}
