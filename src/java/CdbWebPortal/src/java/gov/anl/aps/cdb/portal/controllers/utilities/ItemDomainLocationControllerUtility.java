/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.controllers.utilities;

import gov.anl.aps.cdb.common.exceptions.CdbException;
import gov.anl.aps.cdb.portal.constants.ItemDomainName;
import gov.anl.aps.cdb.portal.model.db.beans.ItemDomainLocationFacade;
import gov.anl.aps.cdb.portal.model.db.entities.Item;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainLocation;
import gov.anl.aps.cdb.portal.model.db.entities.ItemElement;
import gov.anl.aps.cdb.portal.model.db.entities.UserInfo;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author darek
 */
public class ItemDomainLocationControllerUtility extends ItemControllerUtility<ItemDomainLocation, ItemDomainLocationFacade> {   
    
    private static final Logger logger = LogManager.getLogger(ItemDomainLocationControllerUtility.class.getName());

    @Override
    public boolean isEntityHasQrId() {
        return true; 
    }

    @Override
    public boolean isEntityHasName() {
        return true;
    }

    @Override
    public boolean isEntityHasProject() {
        return false;
    }

    @Override
    public String getDefaultDomainName() {
        return ItemDomainName.location.getValue(); 
    }

    @Override
    protected ItemDomainLocationFacade getItemFacadeInstance() {
        return ItemDomainLocationFacade.getInstance(); 
    }
    
    @Override
    public String getDerivedFromItemTitle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
    @Override
    public String getEntityTypeName() {
        return "location";
    }
    
    @Override
    protected ItemDomainLocation instenciateNewItemDomainEntity() {
        return new ItemDomainLocation();
    }
    
    public void updateParentForItem(ItemDomainLocation item, Item newParentItem, UserInfo userInfo) throws CdbException {
        if (newParentItem instanceof ItemDomainLocation == false) {
            return;
        }
        ItemDomainLocation newParent = (ItemDomainLocation) newParentItem;

        ItemDomainLocation ittrParentItem = newParent;
        while (ittrParentItem != null) {            
            if (item.equals(ittrParentItem)) {
                String message = "Cannot set location of item as itself or its child.";
                logger.error(message);
                throw new CdbException(message);                 
            }
            
            ittrParentItem = ittrParentItem.getParentItem();
        }

        ItemElement member = item.getParentItemElement();
        List<ItemElement> itemElementMemberList = item.getItemElementMemberList();                   

        if (member != null) {            
            String elementName = generateUniqueElementNameForItem(newParent);

            member.setName(elementName);
            member.setParentItem(newParent);
        } else if (itemElementMemberList.isEmpty()) {
            ItemElement createItemElement = null;            
            
            createItemElement = createItemElement(newParent, userInfo);             
            createItemElement.setContainedItem(item);
            itemElementMemberList.add(createItemElement); 
        } else {
            String message = "Cannot update parent, item does not have one membership."; 
            logger.error(message);
            throw new CdbException(message); 
        }
    }
    
}
