package lowleveldesigns.socialmediafeed;

import java.time.LocalDateTime;
import java.util.*;

/*
enums
* */
enum PostStatus {
    ACTIVE,
    DELETED;
}

/*
classes
* */
/*

User
Comment
Post
NotificationService
SocialMediaFeed

* */

/*
User
    knows:
        userId
        name
        email
        phoneNo
    does:
        nothing (data carrier)
* */
class User {
    private String userId;
    private String name;
    private String email;
    private int phoneNo;

    public User (String userId, String name, String email, int phoneNo) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phoneNo = phoneNo;
    }

    public String getUserId () {
        return userId;
    }

    public String getName () {
        return name;
    }

    public String getEmail () {
        return email;
    }

    public int getPhoneNo () {
        return phoneNo;
    }
}

/*
Comment
    knows:
        commentId
        commentBody
        User
    does:
        nothing (data carrier)
* */
class Comment {
    private String commentId;
    private String commentBody;
    private User user;

    public Comment (String commentId, String commendBody, User user) {
        this.commentId = commentId;
        this.commentBody = commentBody;
        this.user = user;
    }

    public String getCommentId () {
        return commentId;
    }

    public String getCommentBody () {
        return commentBody;
    }

    public User getUser () {
        return user;
    }
}

/*
Post
    knows:
        postId
        User
        postBody
        timestamp
        Set<User> -> likes
        List<Comment>
        PostStatus
    does:
        like(User)
        removeLike(User)
        addComment(Comment)
        removeComment(Comment)
        updateStatus(PostStatus)
* */
class Post {
    private String postId;
    private User author;
    private String postBody;
    private LocalDateTime timestamp;
    private Set<String> likers;
    private List<Comment> comments;
    private PostStatus postStatus;

    public Post (String postId, User author, String postBody) {
        this.postId = postId;
        this.author = author;
        this.postBody = postBody;
        this.timestamp = LocalDateTime.now();
        this.likers = new HashSet<>();
        this.comments = new ArrayList<>();
        this.postStatus = PostStatus.ACTIVE;
    }

    public String getPostId () {
        return postId;
    }

    public User getAuthor () {
        return author;
    }

    public String postBody () {
        return postBody;
    }

    public LocalDateTime getTimestamp () {
        return timestamp;
    }

    public int getLikesCount () {
        return likers.size();
    }

    public List<Comment> getComments () {
        return comments;
    }

    public PostStatus getPostStatus () {
        return postStatus;
    }

    public void updateStatus (PostStatus postStatus) {
        this.postStatus = postStatus;
    }

    public void like (User user) {
        likers.add(user.getUserId());
    }

    public void removeLike (User user) {
        likers.remove(user.getUserId());
    }

    public void addComment (Comment comment) {
        comments.add(comment);
    }

    public void removeComment (Comment comment) {
        comments.remove(comment);
    }
}

/*
NotificationService
    knows:
    does:
        notifyFollow(User follower, User followee)
        notifyLike(User, Post)
* */
class NotificationService {
    public void notifyFollow(User follower, User followee) {
        System.out.println("Notification sent to follower: " + follower.getName() + " and followee: " + followee.getName());
    }

    public void notifyLike (User user, Post post) {
        System.out.println("Notification sent ot post author: " + post.getAuthor().getName() + " about liker: " + user.getName() + " on post: " + post.getPostId());
    }
}

/*
SocialMediaFeed
    knows:
        List<User>
        Map<String, List<Post>> -> userId -> List of Posts
        Map<String, Set<User>> -> userId -> Set of followees
        Map<String, set<User>> -> userId -> Set of followers
        NotificationService
    does:
        addUser(User)
        removeUser(User)
        addPost(Post)
        removePost(Post)
        addComment(Post, Comment)
        removeComment(Post, Comment)
        like(User, Post)
        removeLike(User, Post)
        follow(User follower, User followee)
        unfollow(User follower, User followee)
        getFeed(User) -> List<Post>
* */
public class SocialMediaFeed {
    private List<User> users;
    private Map<String, List<Post>> posts;
    private Map<String, Set<User>> followees;
    private Map<String, Set<User>> followers;
    private NotificationService notificationService;

    public SocialMediaFeed (NotificationService notificationService) {
        this.users = new ArrayList<>();
        this.posts = new HashMap<>();
        this.followees  = new HashMap<>();
        this.followers = new HashMap<>();
        this.notificationService = notificationService;
    }

    public void addUser (User user) {
        users.add(user);
    }

    public void removeUser (User user) {
        users.remove(user);

        String key = user.getUserId();
        posts.remove(key);
        followees.remove(key);
        followers.remove(key);
    }

    public void addPost (Post post) {
        String key = post.getAuthor().getUserId();
        if (!posts.containsKey(key)) {
            posts.put(key, new ArrayList<>());
        }
        posts.get(key).add(post);
    }

    public void removePost (Post post) {
        String key = post.getAuthor().getUserId();
        posts.get(key).remove(post);
    }

    public void addComment (Post post, Comment comment) {
        post.addComment(comment);
    }

    public void removeComment (Post post, Comment comment) {
        post.removeComment(comment);
    }

    public void like (User user, Post post) {
        post.like(user);
        notificationService.notifyLike(user, post);
    }

    public void removeLike (User user, Post post) {
        post.removeLike(user);
    }

    public void follow (User followee, User follower) {
        followees.putIfAbsent(follower.getUserId(), new HashSet<>());
        followers.putIfAbsent(followee.getUserId(), new HashSet<>());

        followees.get(follower.getUserId()).add(followee);
        followers.get(followee.getUserId()).add(follower);

        notificationService.notifyFollow(follower, followee);
    }

    public void unfollow (User followee, User follower) {
        if (!followees.containsKey(follower.getUserId())) {
            return;
        }
        followees.get(follower.getUserId()).remove(followee);
        followers.get(followee.getUserId()).remove(follower);
    }

    public List<Post> getFeed (User user) {
        // 1. Get the List of Followees for the user
        // 2. Get the 20 most recent posts from each followee
        // 3. Put it in a PriorityQueue<Post> sorted by timestamp desc (latest one first) -> max heap
        Set<User> currFollowees = followees.getOrDefault(user.getUserId(), new HashSet<>());
        PriorityQueue<Post> pq = new PriorityQueue<>((a, b) -> {
           if(!a.getTimestamp().equals(b.getTimestamp())) {
               return b.getTimestamp().compareTo(a.getTimestamp());
           }
           return b.getPostId().compareTo(a.getPostId());
        });

        for(User followee: currFollowees) {
            List<Post> followeePosts = posts.get(followee.getUserId());
            int maxPosts = Math.min(followeePosts.size(), 20);
            for(int i = 0; i < maxPosts; i++) {
                Post post = followeePosts.get(followeePosts.size() - 1 - i);
                if (post.getPostStatus() == PostStatus.DELETED) {
                    continue;
                }
                pq.offer(post);
            }
        }

        List<Post> feed = new ArrayList<>();
        while(!pq.isEmpty() && feed.size() < 20) {
            feed.add(pq.poll());
        }

        return feed;
    }
}

/*

User has-a Set of Users representing the followees
User has-a Set of Users representing the followers

Comment has-a User

Post has-a User author
Post has-a List of Users representing the likes
Post has-a List of Comments
Post has-a PostStatus

SocialMediaFeed has-a List of Users
SocialMediaFeed has-a List of Posts
SocialMediaFeed has-a NotificationService

* */
