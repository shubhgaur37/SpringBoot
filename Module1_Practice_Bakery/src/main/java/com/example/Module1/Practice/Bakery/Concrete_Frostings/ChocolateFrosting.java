package com.example.Module1.Practice.Bakery.Concrete_Frostings;

import com.example.Module1.Practice.Bakery.Frosting;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "frosting.type", havingValue = "chocolate")
public class ChocolateFrosting implements Frosting {
    @Override
    public String getFrostingType() {
        return "Chocolate";
    }
}
