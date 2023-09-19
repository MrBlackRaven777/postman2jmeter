package il.blackraven.postman2jmeter.service;

import il.blackraven.postman2jmeter.dto.P2JRequest;
import il.blackraven.postman2jmeter.dto.P2JResponse;

public interface IP2J {
    P2JResponse convert(P2JRequest request);
}
