package org.lecturestudio.web.api.roles;

public class CoDozent extends Role{

    /**
     * creates a new role and sets its name
     *
     * @param name
     */
    public CoDozent(String name) {
        super(name);
    }

    @Override
    public boolean createCourse() {
        return false;
    }

    @Override
    public boolean permissionChat() {
        return true;
    }

    @Override
    public boolean permissionLaserPointer() {
        return true;
    }

    @Override
    public boolean permissionStreaming(){ return true; }
}
