package pss.rookscore.core.webapi;


import ca.cgwpope.jsonobjectwrapper.IJSONObjectWrapper;

public interface RemotePlayer extends IJSONObjectWrapper{
    public int getId();
    public void setId(int id);


    public int getPlayerId();
    public void setPlayerId(int id);

    public String getFirstName();
    public void setFirstName(String firstName);

    public String getLastName();
    public void setLastName(String lastName);
}
