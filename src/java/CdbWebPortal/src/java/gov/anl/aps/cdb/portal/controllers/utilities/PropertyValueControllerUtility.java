/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.controllers.utilities;

import gov.anl.aps.cdb.portal.model.db.beans.PropertyValueFacade;
import gov.anl.aps.cdb.portal.model.db.entities.PropertyValue;

/**
 *
 * @author darek
 */
public class PropertyValueControllerUtility extends CdbEntityControllerUtility<PropertyValue, PropertyValueFacade> {

    @Override
    protected PropertyValueFacade getEntityDbFacade() {
        return PropertyValueFacade.getInstance(); 
    }        

    @Override
    public String getEntityTypeName() {
        return "propertyValue";
    }
    
}
