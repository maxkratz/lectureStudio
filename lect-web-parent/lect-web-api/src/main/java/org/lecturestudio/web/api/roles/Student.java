package org.lecturestudio.web.api.roles;

public class Student extends Role{

    /**
     * creates a new role and sets its name
     *
     * @param name
     */
    public Student(String name) {
        super(name);
    }

    public static boolean laserpointer;
    public static boolean chat;
    public static boolean streaming;
    @Override
    public boolean createCourse() {
        return false;
    }

    @Override
    public boolean permissionChat() {
        return chat;
    }

    @Override
    public boolean permissionLaserPointer() {
        return laserpointer;
    }

    @Override
    public boolean permissionStreaming(){ return streaming; }
}
