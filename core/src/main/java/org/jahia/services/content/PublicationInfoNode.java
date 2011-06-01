package org.jahia.services.content;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author toto
 * Date: Aug 26, 2010
 * Time: 5:31:55 PM
 */
public class PublicationInfoNode implements Serializable {
    
    private static final long serialVersionUID = 8826165087616513109L;
    
    private String uuid;
    private String path;
    private int status;
    private boolean locked;
    private Map<String,Boolean> canPublish;
    private List<PublicationInfoNode> child = new LinkedList<PublicationInfoNode>();
    private List<PublicationInfo> references = new LinkedList<PublicationInfo>();
    private List<String> pruned;

    public PublicationInfoNode() {
        super();
        canPublish = new HashMap<String, Boolean>(3);
    }

    public PublicationInfoNode(String uuid, String path) {
        this();
        this.uuid = uuid;
        this.path = path;
    }

    public PublicationInfoNode(String uuid, String path, int status) {
        this(uuid, path);
        this.status = status;
    }

    public String getUuid() {
        return uuid;
    }

    public String getPath() {
        return path;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isCanPublish(String language) {
        Boolean aBoolean = canPublish.get(language);
        return aBoolean !=null?aBoolean:false;
    }

    public void setCanPublish(boolean canPublish,String language) {
        this.canPublish.put(language,canPublish);
    }

    public List<PublicationInfoNode> getChildren() {
        return child;
    }

    public List<PublicationInfo> getReferences() {
        return references;
    }

    public List<String> getPruned() {
        return pruned;
    }

    public void addChild(PublicationInfoNode node) {
        child.add(node);
    }

    public PublicationInfoNode addChild(String uuid, String path) {
        final PublicationInfoNode node = new PublicationInfoNode(uuid, path);
        child.add(node);
        return node;
    }

    public void addReference(PublicationInfo ref) {
        references.add(ref);
    }

    public PublicationInfo addReference(String uuid, String path) {
        final PublicationInfo ref = new PublicationInfo(uuid, path);
        references.add(ref);
        return ref;
    }
}
