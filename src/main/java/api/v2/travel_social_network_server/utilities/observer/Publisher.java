package api.v2.travel_social_network_server.utilities.observer;

public interface Publisher<T> {
    void subscribe(Subscriber<T> subscriber);
    void unsubscribe(Subscriber<T> subscriber);
    void publish(T  message);
}