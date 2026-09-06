package api.v2.travel_social_network_server.utilities.observer;

import api.v2.travel_social_network_server.entities.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserPublisherManagement implements Publisher<User>{
    private final List<Subscriber<User>> subscribers = new ArrayList<>();

    public void subscribe(Subscriber<User> s) {
        subscribers.add(s);
    }

    public void unsubscribe(Subscriber<User> s) {
        subscribers.remove(s);
    }

    public void publish(User user) {
        // gửi tới tất cả subscribers (đồng bộ)
        for (Subscriber<User> s : subscribers) {
            s.onMessage(user);
        }
    }
}
