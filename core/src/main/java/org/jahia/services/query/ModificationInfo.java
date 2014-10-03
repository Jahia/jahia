package org.jahia.services.query;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.query.qom.*;

import org.jahia.services.query.QueryModifierAndOptimizerVisitor.TraversingMode;

/**
 * This class is used to gather modification information mainly set during the INITIALIZE_MODE traversing. During traversing in
 * CHECK_FOR_MODIFICATION mode mainly only the modificationNecessary variable is set. The information of this object is then used during
 * the MODIFY_MODE traversing iteration.
 * 
 */
class ModificationInfo {
    private TraversingMode mode = TraversingMode.INITIALIZE_MODE;

    private boolean modificationNecessary = false;

    private Join newJoin = null;

    private QueryObjectModelFactory queryObjectModelFactory = null;

    private QueryObjectModel newQueryObjectModel = null;

    private List<Constraint> newConstraints = new ArrayList<Constraint>();

    /**
     * Constructor setting the QueryObjectModelFactory to be used for modifying the query
     * 
     * @param queryObjectModelFactory
     *            to be used for modifying the query
     */
    public ModificationInfo(QueryObjectModelFactory queryObjectModelFactory) {
        super();
        this.queryObjectModelFactory = queryObjectModelFactory;
    }

    /**
     * @return true when modification of the query is found to be necessary otherwise return value is false
     */
    public boolean isModificationNecessary() {
        return modificationNecessary;
    }

    /**
     * Set true when modification of the query is found to be necessary otherwise set false
     * 
     * @param modificationNecessary
     *            true when modification of the query is necessary otherwise false
     */
    public void setModificationNecessary(boolean modificationNecessary) {
        this.modificationNecessary = modificationNecessary;
    }

    /**
     * @return the QueryObjectModelFactory to be used when creating the modified query
     */
    public QueryObjectModelFactory getQueryObjectModelFactory() {
        return queryObjectModelFactory;
    }

    /**
     * @return the new Source, which in any case is a join when adding translation node queries
     */
    public Join getNewJoin() {
        return newJoin;
    }

    /**
     * Set the new Source, which in any case is a join when adding translation node queries
     * 
     * @param newJoin
     *            when a new translation node is added.
     */
    public void setNewJoin(Join newJoin) {
        setModificationNecessary(true);
        this.newJoin = newJoin;
    }

    /**
     * @return the mode of the current iteration
     */
    public TraversingMode getMode() {
        return mode;
    }

    /**
     * Set mode for current iteration
     */
    public void setMode(TraversingMode mode) {
        this.mode = mode;
    }

    /**
     * @return the modified query object model
     */
    public QueryObjectModel getNewQueryObjectModel() {
        return newQueryObjectModel;
    }

    /**
     * Set the new modified query object model
     * 
     * @param newQueryObjectModel
     *            set after modification
     */
    public void setNewQueryObjectModel(QueryObjectModel newQueryObjectModel) {
        this.newQueryObjectModel = newQueryObjectModel;
    }

    /**
     * @return new constraints, which need to be added with logical conjunction (AND)
     */
    public List<Constraint> getNewConstraints() {
        return newConstraints;
    }
    
    public Source getModifiedSource(Source source) {
        return getNewJoin() != null ? getNewJoin() : source;
    }
}