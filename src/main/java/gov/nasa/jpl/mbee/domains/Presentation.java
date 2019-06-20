package gov.nasa.jpl.mbee.domains;

import java.util.List;

/**
 * @author shakeh
 */
public class Presentation {

    private List<PresentationElement> elements;

    public List<PresentationElement> getElements() {
        return elements;
    }

    public void setElements(List<PresentationElement> elements) {
        this.elements = elements;
    }
}