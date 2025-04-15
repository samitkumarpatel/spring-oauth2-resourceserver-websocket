package net.samitkumar.spring_oauth2_resourceserver_websocket.websocket;

public enum Event {
    CONNECT("User Connected , a public message to all"),
    DISCONNECT("User Disconnect, a public message to all"),
    MESSAGE_TO_ALL("Public message to all users"),
    MESSAGE_FROM_USER("A message from a user to you"),
    MESSAGE_FROM_GROUP("A message from a user to a group you are part of"),
    MESSAGE_TO_USER("Your own message to a user"),
    MESSAGE_TO_GROUP("Your own message to a group");

    public final String purpose;
    Event(String purpose) {
        this.purpose = purpose;
    }
}
