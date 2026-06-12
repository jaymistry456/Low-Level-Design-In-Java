package lowleveldesigns.unixfilesystem;

import java.util.ArrayList;
import java.util.List;

/*
enums
* */
enum EntityType {
    FILE,
    DIRECTORY;
}

/*
classes
* */
/*
Entity (abstract class)
File (extends Entity)
Directory(extends Entity)
UnixFileSystem
* */

/*
Entity
    knows:
        name
        EntityType
    does:
        getName() -> String
        setName(String)
        getEntityType() -> EntityType
        getSize() -> double
        abstract display(int indentLevel)
        abstract search(name) -> List<Entity>
* */
abstract class Entity {
    private String name;
    private EntityType entityType;

    public Entity(String name, EntityType entityType) {
        this.name = name;
        this.entityType = entityType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    abstract double getSize();
    abstract void display(int indentLevel);
    abstract List<Entity> search(String name);
}

/*
File
    knows:
        size
    does:
        getSize() -> double
        display(int indentLevel)
        search(name) -> List<Entity>
* */
class File extends Entity {
    private double size;

    public File(String name, double size) {
        super(name, EntityType.FILE);
        this.size = size;
    }

    @Override
    public double getSize() {
        return size;
    }

    @Override
    public void display(int indentLevel) {
        System.out.println(" ".repeat(indentLevel) + getName() + " (" + getSize() + ")");
    }

    @Override
    public List<Entity> search(String name) {
        if(getName().equals(name)) {
            return List.of(this);
        }
        return new ArrayList<>();
    }
}

/*
Directory
    knows:
        List<Entity>
    does:
        addChild(Entity)
        removeChild(Entity)
        getSize() -> double
        display(int indentLevel)
        search(name) -> List<Entity>
* */
class Directory extends Entity {
    private List<Entity> entities;

    public Directory(String name) {
        super(name, EntityType.DIRECTORY);
        this.entities = new ArrayList<>();
    }

    public void addChild(Entity entity) {
        entities.add(entity);
    }

    public void removeChild(Entity entity) {
        entities.remove(entity);
    }

    @Override
    public double getSize() {
        double result = 0;

        for(Entity entity: entities) {
            result += entity.getSize();
        }

        return result;
    }

    @Override
    public void display(int indentLevel) {
        System.out.println(" ".repeat(indentLevel) + getName() + "/");
        for(Entity entity: entities) {
            entity.display(indentLevel + 1);
        }
    }

    @Override
    public List<Entity> search(String name) {
        List<Entity> result = new ArrayList<>();

        if(super.getName().equals(name)) result.add(this);

        for(Entity entity: entities) {
            result.addAll(entity.search(name));
        }
        return result;
    }
}

/*
UnixFileSystem
    knows:
        Directory root
    does:
        createEntity(Directory parent, Entity child)
        deleteEntity(Directory parent, Entity child)
        search(entityName) -> List<Entity>
        getSize(Entity) -> double
        rename(Entity, name)
        display(Entity)
* */

public class UnixFileSystem {
    private Directory root;

    public UnixFileSystem(Directory root) {
        this.root = root;
    }

    public void createEntity(Directory parent, Entity child) {
        parent.addChild(child);
    }

    public void deleteEntity(Directory parent, Entity child) {
        parent.removeChild(child);
    }

    public List<Entity> search(String name) {
        return root.search(name);
    }

    public double getSize(Entity entity) {
        return entity.getSize();
    }

    public void rename(Entity entity, String name) {
        entity.setName(name);
    }

    public void display(Entity entity) {
        entity.display(0);
    }
}

/*

Entity has-a EntityType

File is-a Entity

Directory is-a Entity
Directory has-a List of Entity

UnixFileSystem has-a Entity root

* */