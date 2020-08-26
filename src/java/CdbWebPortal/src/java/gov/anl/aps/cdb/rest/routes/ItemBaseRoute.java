/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.rest.routes;

import gov.anl.aps.cdb.common.exceptions.AuthorizationError;
import gov.anl.aps.cdb.common.exceptions.ObjectNotFound;
import gov.anl.aps.cdb.portal.model.db.beans.ItemFacade;
import gov.anl.aps.cdb.portal.model.db.entities.Item;
import gov.anl.aps.cdb.portal.model.db.entities.UserInfo;
import gov.anl.aps.cdb.portal.utilities.AuthorizationUtility;
import gov.anl.aps.cdb.rest.authentication.User;
import java.security.Principal;
import javax.ejb.EJB;
import javax.ws.rs.PathParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author djarosz
 */
public abstract class ItemBaseRoute extends BaseRoute {
    
    @EJB
    ItemFacade itemFacade;    
    
    private static final Logger LOGGER = LogManager.getLogger(ItemBaseRoute.class.getName());        
    
    public Item getItemByIdBase(@PathParam("id") int id) throws ObjectNotFound {        
        Item findById = itemFacade.findById(id);
        if (findById == null) {
            ObjectNotFound ex = new ObjectNotFound("Could not find item with id: " + id);
            LOGGER.error(ex);
            throw ex; 
        }
        return findById;
    }
    
    protected UserInfo verifyCurrentUserPermissionForItem(Item item) throws AuthorizationError {
        UserInfo updatedByUser = getCurrentRequestUserInfo();
        
        if (!verifyUserPermissionForItem(updatedByUser, item)) {            
            AuthorizationError ex = new AuthorizationError("User does not have permission to update property value for the item");
            LOGGER.error(ex);
            throw ex; 
        }
        
        return updatedByUser; 
    }
    
    protected boolean verifyUserPermissionForItem(UserInfo user, Item item) {        
        if (user != null) {
            if (isUserAdmin(user)) {
                return true;
            }
            return AuthorizationUtility.isEntityWriteableByUser(item, user);
        }
        return false;
    }
    
    protected UserInfo getCurrentRequestUserInfo() {
        Principal userPrincipal = securityContext.getUserPrincipal();
        if (userPrincipal instanceof User) {
            UserInfo user = ((User) userPrincipal).getUser();
            return user;
        }
        return null;
    }
    
}
