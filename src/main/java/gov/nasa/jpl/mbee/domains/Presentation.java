package gov.nasa.jpl.mbee.domains;

/**
 * @author shakeh
 */
public class Presentation {

    private String ticket;

    private String presentation_id;

    public String getTicket() { return ticket; }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getPresentation_id() {
        return presentation_id;
    }

    public void setPresentation_id(String presentation_id) {
        this.presentation_id = presentation_id;
    }
}