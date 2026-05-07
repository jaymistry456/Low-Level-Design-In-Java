package lowleveldesigns.notificationsystem;

/*
enums
* */
// NotificationStatus

/*
classes
* */
// User
// NotificationChannel (interface)
// EmailNotification implements NotificationChannel
// SMSNotification implements NotificationChannel
// PushNotification implements NotificationChannel
// Notification
// NotificationSystem


import java.util.ArrayList;
import java.util.List;

/*
enums
* */
enum NotificationStatus {
    PENDING,
    SENT,
    FAILED;
}

/*
classes
* */
/*
User
    knows:
        userId
        name
        List<NotificationChannel>
    does:
        subscribe(NotificationChannel)
        unsubscribe(NotificationChannel)
* */
class User {
    private String userId;
    private String name;
    private List<NotificationChannel> channels;

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.channels = new ArrayList<>();
    }

    public void subscribe(NotificationChannel channel) {
        channels.add(channel);
    }

    public void unsubscibe(NotificationChannel channel) {
        channels.remove(channel);
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public List<NotificationChannel> getChannels() {
        return channels;
    }
}

/*
NotificationChannel (interface)
    knows:
        nothing
    does:
        send(Notification)
* */
interface NotificationChannel {
    boolean send(Notification notification);
}

/*
EmailNotification implements NotificationChannel
    knows:
        String emailId
    does:
        send(Notification)
* */
class EmailNotification implements NotificationChannel {
    private String emailId;

    public EmailNotification(String emailId) {
        this.emailId = emailId;
    }

    public boolean send(Notification notification) {
        try {
            System.out.println("Notification sent using Email notification.");
            return true;
        } catch (Exception e) {
            System.out.println("Error occurred while sending the notification via Email");
            return false;
        }
    }
}

/*
SMSNotification implements NotificationChannel
    knows:
        int phoneNo
    does:
        send(Notification)
* */
class SMSNotification implements NotificationChannel {
    private int phoneNo;

    public SMSNotification(int phoneNo) {
        this.phoneNo = phoneNo;
    }

    public boolean send(Notification notification) {
        try {
            System.out.println("Notification sent using SMS notification.");
            return true;
        } catch (Exception e) {
            System.out.println("Error occurred while sending the notification via SMS");
            return false;
        }
    }
}

/*
PushNotification implements NotificationChannel
    knows:
        String deviceId
    does:
        send(Notification)
* */
class PushNotification implements NotificationChannel {
    private String deviceId;

    public PushNotification(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean send(Notification notification) {
        try {
            System.out.println("Notification sent using Push notification.");
            return true;
        } catch (Exception e) {
            System.out.println("Error occurred while sending the notification via Push");
            return false;
        }
    }
}

/*
Notification
    know:
        String title
        String body
        NotificationStatus
    does:
        updateStatus(NotificationStatus)
* */
class Notification {
    private String title;
    private String body;
    private NotificationStatus status;

    public Notification(String title, String body) {
        this.title = title;
        this.body = body;
        this.status = NotificationStatus.PENDING;
    }

    public void updateStatus(NotificationStatus status) {
        this.status = status;
    }

    public NotificationStatus getStatus() {
        return status;
    }
}

/*
NotificationSystem
    knows:
        List<User>
    does:
        addUser(User)
        removeUser(User)
        sendToUser(Notification, User)
        broadcast(Notification)
* */

public class NotificationSystem {
    private List<User> users;

    private final int RETRIES = 5;

    public NotificationSystem() {
        this.users = new ArrayList<>();
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    public void sendToUser(Notification notification, User user) {
        boolean anySuccess = false;

        for(NotificationChannel channel: user.getChannels()) {
            boolean sent = false;
            for(int i = 0; i < RETRIES; i++) {
                if(channel.send(notification)) {
                    sent = true;
                    break;
                }
                try {
                    Thread.sleep((long) Math.pow(2, i) * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if(sent) anySuccess = true;
        }

        if(anySuccess) {
            notification.updateStatus(NotificationStatus.SENT);
        }
        else {
            notification.updateStatus(NotificationStatus.FAILED);
            System.out.println("Notification failed to send to the user: " + user.getName());
        }
    }

    public void broadcast(Notification notification) {
        users.forEach(
                user -> sendToUser(notification, user)
        );
    }
}

/*

NotificationSystem has-a List of Users

EmailNotification is-a NotificationChannel
SMSNotification is-a NotificationChannel
PushNotification is-a NotificationChannel

User has-a List of NotificationChannels

* */