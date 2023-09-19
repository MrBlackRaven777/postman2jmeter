package il.blackraven.postman2jmeter.controllers;

import il.blackraven.postman2jmeter.dto.P2JRequest;
import il.blackraven.postman2jmeter.dto.P2JResponse;
import il.blackraven.postman2jmeter.service.IP2J;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class P2JController {

    @Autowired
    IP2J service;

    @PostMapping("/convert")
    public P2JResponse convert(@RequestBody P2JRequest request) {
        return service.convert(request);
    }
}
