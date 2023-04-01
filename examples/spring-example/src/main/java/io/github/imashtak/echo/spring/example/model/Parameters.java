package io.github.imashtak.echo.spring.example.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Parameters {

    private String first;
    private String second;
    private String third;
    private String forth;

    private Parameters() {
    }

    private static Parameters instance;

    public synchronized static Parameters instance() {
        if (instance == null) {
            instance = new Parameters();
        }
        return instance;
    }
}
