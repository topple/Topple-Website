package org.drupal.project.async_command.deprecated;

@Deprecated
public class CommandDeprecated {
    private int id;
    private String command;
    private int uid;
    private int eid;
    private int created;

    public CommandDeprecated(int id, String command, int uid, int eid, int created) {
        this.id = id;
        this.command = command;
        this.uid = uid;
        this.eid = eid;
        this.created = created;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getEid() {
        return eid;
    }

    public void setEid(int eid) {
        this.eid = eid;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }
}
