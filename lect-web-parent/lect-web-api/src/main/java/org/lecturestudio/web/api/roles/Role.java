package org.lecturestudio.web.api.roles;

public abstract class Role {
    private String name;

    /**
     * creates a new role and sets its name
     * @param name
     */
    public Role(String name) {
        this.name = name;
    }



    public String getName (){
        return name;
    }

    /**
     * checks if the user of this role has a permission to create a course
     * @return boolean value: "true" if the user can create a course, otherwise "false"
     */
    public abstract boolean createCourse();

    /**
     * checks if the user of the role has a permission to set up the chat
     * @return boolean value: "true" if the user can set it up, otherwise "false"
     */
    public abstract boolean permissionChat();

    /**
     * checks if the user of the role can set up the Laser Pointer
     * @return boolean value: "true" if the user can do it, otherwise "false"
     */
    public abstract boolean permissionLaserPointer();

    /**
     * checks if the user of the role can stream on the lecture
     * @return boolean value: "true" if the user can do it, otherwise "false"
     */
    public abstract boolean permissionStreaming();


}
