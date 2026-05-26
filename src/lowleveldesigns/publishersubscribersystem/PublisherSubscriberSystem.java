package lowleveldesigns.publishersubscribersystem;

/*
classes
* */
/*
Message
MessageHandler (interface)
Client
Topic
PublisherSubscriberSystem
* */

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/*
Message
    knows:
        messageId
        publisherId
        content
        timestamp
    does:
        nothing (data carrier)
* */
class Message {
    private String messageId;
    private String publisherId;
    private String content;
    private LocalDateTime timestamp;

    public Message(String messageId, String publisherId, String content) {
        this.messageId = messageId;
        this.publisherId = publisherId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public String getMessageId() {
        return messageId;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

/*
MessageHandler (interface)
    knows:
    does:
        onMessage(Message)
* */
interface MessageHandler {
    void onMessage(Message message);
}

/*
EmailMessageHandler implements MessageHandler (example implementation)
    knows:
    does:
        onMessage(Message)
* */
class EmailMessageHandler implements MessageHandler {
    @Override
    public void onMessage(Message message) {
        System.out.println("Message sent by Email.");
    }
}

/*
LoggingMessageHandler implements MessageHandler (example implementation)
    knows:
    does:
        onMessage(Message)
* */
class LoggingMessageHandler implements MessageHandler {
    @Override
    public void onMessage(Message message) {
        System.out.println("Message logged to console.");
    }
}

/*
Client
    knows:
        clientId
        MessageHandler
    does:
        onMessage(Message)
* */
class Client {
    private String clientId;
    private MessageHandler handler;

    public Client(String clientId, MessageHandler handler) {
        this.clientId = clientId;
        this.handler = handler;
    }

    public String getClientId() {
        return clientId;
    }

    public void onMessage(Message message) {
        handler.onMessage(message);
    }
}

/*
Topic
    knows:
        topicId
        Set<Client> (subscribers)
        ReentrantLock
    does:
        subscribe(Client)
        unsubscribe(Client)
        deliverMessage(Message)
* */
class Topic {
    private String topicId;
    private Set<Client> subscribers;
    private ReentrantLock lock = new ReentrantLock();

    public Topic(String topicId) {
        this.topicId = topicId;
        this.subscribers = new HashSet<>();
    }

    public void subscribe(Client subscriber) {
        lock.lock();
        try {
            subscribers.add(subscriber);
        } finally {
            lock.unlock();
        }
    }

    public void unsubscribe(Client subscriber) {
        lock.lock();
        try {
            subscribers.remove(subscriber);
        } finally {
            lock.unlock();
        }
    }

    public void deliverMessage(Message message) {
        Set<Client> snapshot;
        lock.lock();
        try {
            snapshot = new HashSet<>(subscribers);
        } finally {
            lock.unlock();
        }

        // Deliver outside to not block the Topic
        for(Client subscriber: snapshot) {
            subscriber.onMessage(message);
        }
    }
}

/*
PublisherSubscriberSystem
    knows:
        List<Client>
        List<Topic>
    does:
        addClient(Client)
        removeClient(Client)
        addTopic(Topic)
        removeTopic(Topic)
        publish(Message, Topic)
        subscribe(Client, Topic)
        unsubscribe(Client, Topic)
* */
public class PublisherSubscriberSystem {
    private List<Client> clients;
    private List<Topic> topics;

    public PublisherSubscriberSystem() {
        this.clients = new ArrayList<>();
        this.topics = new ArrayList<>();
    }

    public void addClient(Client client) {
        clients.add(client);
    }

    public void removeClients(Client client) {
        clients.remove(client);
    }

    public void addTopic(Topic topic) {
        topics.add(topic);
    }

    public void removeTopic(Topic topic) {
        topics.remove(topic);
    }

    public void publish(Message message, Topic topic) {
        topic.deliverMessage(message);
    }

    public void subscribe(Client subscriber, Topic topic) {
        topic.subscribe(subscriber);
    }

    public void unsubscribe(Client client, Topic topic) {
        topic.unsubscribe(client);
    }
}

/*

EmailMessageHandler is-a MessageHandler
LoggingMessageHandler is-a MessageHandler

Client has-a MessageHandler

Topic has-a Set of Clients (subscribers)

PublisherSubscriberSystem has-a List of Clients
PublisherSubscriberSystem has-a List of Topics

* */
