package lowleveldesigns.taskmanagementsystem;

import java.time.LocalDateTime;
import java.util.*;

/*
enums
* */
enum TaskStatus {
    TODO,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;
}

enum TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;
}

/*
classes
* */
/*
User
Comment
Task
TaskFilter
Project
TaskManagementSystem
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
    private String phoneNo;

    public User (String userId, String name, String email, String phoneNo) {
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

    public String getPhoneNo () {
        return phoneNo;
    }
}

/*
Comment
    knows:
        commentId
        commentBody
        User
        timestamp
    does:
        nothing (data carrier)
* */
class Comment {
    private String commentId;
    private String commentBody;
    private User commenter;
    private LocalDateTime timestamp;

    public Comment (String commentId, String commentBody, User commenter) {
        this.commentId = commentId;
        this.commentBody = commentBody;
        this.commenter = commenter;
        this.timestamp = LocalDateTime.now();
    }

    public String getCommentId () {
        return commentId;
    }

    public String getCommentBody () {
        return commentBody;
    }

    public User getCommenter () {
        return commenter;
    }

    public LocalDateTime getTimestamp () {
        return timestamp;
    }
}

/*
Task
    knows:
        taskId
        taskName
        taskDescription
        TaskStatus
        TaskPriority
        User (assignee)
        List<Comment>
        List<Task> subTasks
        LocalDateTime (due date)
    does:
        changeAssignee(User)
        updateStatus(TaskStatus)
        updatePriority(TaskPriority)
        addComment(Comment)
        removeComment(Comment)
        addSubtask(Task)
        removeSubtask(Task)
* */
class Task {
    private String taskId;
    private String taskName;
    private String taskDescription;
    private TaskStatus status;
    private TaskPriority priority;
    private User assignee;
    private List<Comment> comments;
    private List<Task> subTasks;
    private LocalDateTime dueDate;

    public Task (
        String taskId,
        String taskName,
        String taskDescription,
        TaskPriority priority,
        User assignee,
        LocalDateTime dueDate
    ) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.status = TaskStatus.TODO;
        this.priority = priority;
        this.assignee = assignee;
        this.comments = new ArrayList<>();
        this.subTasks = new ArrayList<>();
        this.dueDate = dueDate;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public User getAssignee() {
        return assignee;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public List<Task> getSubTasks() {
        return subTasks;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void changeAssignee (User assignee) {
        this.assignee = assignee;
    }

    public void updateStatus (TaskStatus status) {
        this.status = status;
    }

    public void updatePriority (TaskPriority priority) {
        this.priority = priority;
    }

    public void addComment (Comment comment) {
        comments.add(comment);
    }

    public void removeComment (Comment comment) {
        comments.remove(comment);
    }

    public void addSubtask (Task subtask) {
        subTasks.add(subtask);
    }

    public void removeSubtask (Task subtask) {
        subTasks.remove(subtask);
    }
}

/*
TaskFilter
    knows:
        TaskStatus
        TaskPriority
        User (assignee)
        apply(List<Task>) -> List<Task>
        TaskFilterBuilder
            knows:
                TaskStatus
                TaskPriority
                User (assignee)
            does:
                withStatus(TaskStatus) -> TaskFilterBuilder
                withPriority(TaskPriority) -> TaskFilterBuilder
                withAssignee(User) -> TaskFilterBuilder
                build() -> TaskFilter
* */
class TaskFilter {
    private TaskStatus status;
    private TaskPriority priority;
    private User assignee;

    private TaskFilter (TaskFilterBuilder builder) {
        this.status = builder.status;
        this.priority = builder.priority;
        this.assignee = builder.assignee;
    }

    public List<Task> apply (List<Task> tasks) {
        return tasks.stream()
                .filter(t -> (status == null || status == t.getStatus()))
                .filter(t -> (priority == null || priority == t.getPriority()))
                .filter(t -> (assignee == null || Objects.equals(assignee.getUserId(), t.getAssignee().getUserId())))
                .toList();
    }

    public static class TaskFilterBuilder {
        private TaskStatus status;
        private TaskPriority priority;
        private User assignee;

        public TaskFilterBuilder () {}

        public TaskFilterBuilder withStatus (TaskStatus status) {
            this.status = status;
            return this;
        }

        public TaskFilterBuilder withPriority (TaskPriority priority) {
            this.priority = priority;
            return this;
        }

        public TaskFilterBuilder withAssignee (User assignee) {
            this.assignee = assignee;
            return this;
        }

        public TaskFilter build () {
            return new TaskFilter(this);
        }
    }

}

/*
Project
    knows:
        projectId
        projectName
        projectDescription
        User (creator)
        List<User> (members)
        List<Task>
    does:
        isMember(User) -> boolean
        addUser(User)
        removeUser(User)
        addTask(Task)
        removeTask(Task)
        updateTaskStatus(Task, TaskStatus)
        addComment(Task, Comment)
        removeComment(Task, Comment)
* */
class Project {
    private String projectId;
    private String projectName;
    private String projectDescription;
    private User creator;
    private List<User> members;
    private List<Task> tasks;
    
    public Project (String projectId, String projectName, String projectDescription, User creator) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.creator = creator;
        this.members = new ArrayList<>();
        this.members.add(creator);
        this.tasks = new ArrayList<>();
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public User getCreator() {
        return creator;
    }

    public List<User> getMembers() {
        return members;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public boolean isMember (User user) {
        return members.stream()
                .anyMatch(m -> m.getUserId().equals(user.getUserId()));
    }
    
    public void addUser (User member) {
        members.add(member);
    }
    
    public void removeUser (User member) {
        members.remove(member);
    }
    
    public void addTask (Task task) {
        tasks.add(task);
    }
    
    public void removeTask (Task task) {
        tasks.remove(task);
    }
    
    public void updateTaskStatus (Task task, TaskStatus status) {
        task.updateStatus(status);
    }
    
    public void addComment (Task task, Comment comment) {
        task.addComment(comment);
    }
    
    public void removeComment (Task task, Comment comment) {
        task.removeComment(comment);
    }
}

/*
TaskManagementSystem
    knows:
        List<User>
        Map<String, List<Project>>   // userId -> List of Projects
    does:
        addUser(User)
        removeUser(User)
        addProject(User, Project)
        removeProject(User, Project)
        addTask(Project, User, Task)
        removeTask(Project, User, Task)
        updateTaskStatus(Project, User, Task, TaskStatus)
        filterTasks(Project, User, TaskFilter) -> List<Task>
        addComment(Project, User, Task, Comment)
        removeComment(Project, User, Task, Comment)
* */
public class TaskManagementSystem {
    private List<User> users;
    private Map<String, List<Project>> userToProjects;
    
    public TaskManagementSystem () {
        this.users = new ArrayList<>();
        this.userToProjects = new HashMap<>();
    }
    
    public void addUser (User user) {
        users.add(user);
    }
    
    public void removeUser (User user) {
        users.remove(user);
    }
    
    public void addProject (User user, Project project) {
        userToProjects.putIfAbsent(user.getUserId(), new ArrayList<>());
        userToProjects.get(user.getUserId()).add(project);
    }
    
    public void removeProject (User user, Project project) {
        if (userToProjects.containsKey(user.getUserId())) {
            userToProjects.get(user.getUserId()).remove(project);
        }
    }
    
    public void addTask (Project project, User user, Task task) {
        if (!project.isMember(user)) return;
        project.addTask(task);
    }
    
    public void removeTask (Project project, User user, Task task) {
        if (!project.isMember(user)) return;
        project.removeTask(task);
    }
    
    public void updateTaskStatus (Project project, User user, Task task, TaskStatus status) {
        if (!project.isMember(user)) return;
        project.updateTaskStatus(task, status);
    }
    
    public List<Task> filterTasks (Project project, User user, TaskFilter taskFilter) {
        if (!project.isMember(user)) return null;
        return taskFilter.apply(project.getTasks());
    }
    
    public void addComment (Project project, User user, Task task, Comment comment) {
        if (!project.isMember(user)) return;
        project.addComment(task, comment);
    }
    
    public void removeComment (Project project, User user, Task task, Comment comment) {
        if (!project.isMember(user)) return;
        project.removeComment(task, comment);
    }
}

/*

Comment has-a User

Task has-a TaskStatus
Task has-a TaskPriority
Task has-a User (assignee)
Task has-a List of Comments
Task has-a List of Task (subtasks)

Project has-a User (creator)
Project has-a List of Users (members)
Project has-a List of Tasks

TaskManagementSystem has-a List of Users
TaskManagementSystem has-a Map of userId -> List of Projects

* */