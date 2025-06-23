package at.aau.serg.websocketserver.session.actioncard;

public class ActionCard {
    private int id;
    private String handle;
    private String headline;
    private String action;
    private String imageName;
    private String[] reactions;

    public ActionCard(int id, String handle, String headline, String action, String imageName, String[] reactions) {
        this.id = id;
        this.handle = handle;
        this.headline = headline;
        this.action = action;
        this.imageName = imageName;
        this.reactions = reactions;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String[] getReactions() {
        return reactions;
    }

    public void setReactions(String[] reactions) {
        this.reactions = reactions;
    }
}
