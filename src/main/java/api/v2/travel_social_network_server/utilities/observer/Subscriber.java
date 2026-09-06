package api.v2.travel_social_network_server.utilities.observer;

import api.v2.travel_social_network_server.entities.User;

public interface Subscriber<T> {
    void onMessage(T other);
}
