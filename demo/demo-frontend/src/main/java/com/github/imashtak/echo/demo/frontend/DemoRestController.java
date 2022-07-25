package com.github.imashtak.echo.demo.frontend;

import com.github.imashtak.echo.demo.events.AsyncDemoTriggered;
import com.github.imashtak.echo.distributed.EventStorage;
import com.github.imashtak.echo.distributed.Identity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DemoRestController {

    private final EventStorage storage;
    private final Identity identity;

    @PostMapping("/async")
    public ResponseEntity<Void> async(String msg) {
        storage.publish(identity, new AsyncDemoTriggered(msg));
        return ResponseEntity.ok(null);
    }
}
