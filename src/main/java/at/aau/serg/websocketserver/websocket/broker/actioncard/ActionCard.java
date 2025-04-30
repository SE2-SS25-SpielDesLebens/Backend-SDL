package at.aau.serg.websocketserver.websocket.broker.actioncard;

public class ActionCard {
    private String headline, action, imageName;
    private String[] actions;

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

    public String[] getActions() {
        return actions;
    }

    public void setActions(String[] actions) {
        this.actions = actions;
    }

    public ActionCard(String action, String imageName, String[] actions, String headline) {
        this.action = action;
        this.imageName = imageName;
        this.actions = actions;
        this.headline = headline;
    }
}
