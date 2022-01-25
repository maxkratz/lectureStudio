package org.lecturestudio.web.api.roles;

public class Dozent extends Role{


    /**
     * creates the role Dozent and sets its name
     * @param name
     */
    public Dozent(String name) {
        super(name);

    }


    /**
     * creates a new role and sets its name
     *
     * @param name
     */


    @Override
    public boolean createCourse() {
        return true;
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
    public boolean permissionStreaming(){
        return true;
    }

    public void activateLaserPointer(boolean LaserPointerRight){
        Student.laserpointer = LaserPointerRight;
    }

    public void activateChat(boolean ChatRight){
        Student.chat = ChatRight;
    }

    public void activateStreaming(boolean StreamingRight){
        Student.streaming = StreamingRight;
    }
}
