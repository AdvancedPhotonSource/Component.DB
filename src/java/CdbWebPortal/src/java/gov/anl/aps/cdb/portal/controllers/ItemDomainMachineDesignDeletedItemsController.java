/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.controllers;

import gov.anl.aps.cdb.common.constants.CdbRole;
import gov.anl.aps.cdb.common.exceptions.CdbException;
import gov.anl.aps.cdb.portal.controllers.settings.ItemDomainMachineDesignDeletedItemSettings;
import gov.anl.aps.cdb.portal.controllers.settings.ItemDomainMachineDesignSettings;
import gov.anl.aps.cdb.portal.import_export.import_.objects.ValidInfo;
import gov.anl.aps.cdb.portal.model.db.entities.Item;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainMachineDesign;
import gov.anl.aps.cdb.portal.model.db.entities.ItemElement;
import gov.anl.aps.cdb.portal.model.db.entities.UserInfo;
import gov.anl.aps.cdb.portal.utilities.AuthorizationUtility;
import gov.anl.aps.cdb.portal.utilities.SessionUtility;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.ListDataModel;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author craig
 */
@Named(ItemDomainMachineDesignDeletedItemsController.CONTROLLER_NAMED)
@SessionScoped
public class ItemDomainMachineDesignDeletedItemsController extends ItemDomainMachineDesignController {

    public final static String CONTROLLER_NAMED = "itemDomainMachineDesignDeletedItemsController";
    private static final Logger LOGGER = LogManager.getLogger(ItemDomainMachineDesignDeletedItemsController.class.getName());
    
    private final static String pluginItemMachineDesignSectionsName = "itemMachineDesignDeletedItemsDetailsViewSections";

    private String restoreDeletedItemMessage;
    
    private String permanentlyRemoveConfirmationName = null;
    private TreeNode permanentlyRemoveNode = new DefaultTreeNode();
    private String permanentlyRemoveDisplayName = null;
    private String permanentlyRemoveMessage = null;

    @Override
    public String getItemListPageTitle() {
        return "Deleted Machine Elements";
    }

    @Override
    public boolean isDisplayRowExpansionForItem(Item item) {
        return super.isDisplayRowExpansionForItem(item); //To change body of generated methods, choose Tools | Templates.
    }

    public static ItemDomainMachineDesignDeletedItemsController getInstance() {
        if (SessionUtility.runningFaces()) {
            return (ItemDomainMachineDesignDeletedItemsController) SessionUtility.findBean(CONTROLLER_NAMED);
        } else {
            return null;
        }
    }

    @Override
    protected ItemDomainMachineDesignSettings createNewSettingObject() {
        return new ItemDomainMachineDesignDeletedItemSettings(this);
    }

    @Override
    public String getPluginItemMachineDesignSectionsName() {
        return pluginItemMachineDesignSectionsName; 
    }

    @Override
    public void createListDataModel() {
        List<ItemDomainMachineDesign> itemList = getItemList();
        ListDataModel newListDataModel = new ListDataModel(itemList);
        setListDataModel(newListDataModel);
    }
    
    @Override
    public List<ItemDomainMachineDesign> getItemList() {
        return itemDomainMachineDesignFacade.getDeletedItems();
    }

    @Override
    public TreeNode loadMachineDesignRootTreeNode(Boolean isTemplate) {
        TreeNode rootTreeNode = new DefaultTreeNode();
        List<ItemDomainMachineDesign> itemsWithoutParents
                = getItemsWithoutParents();

        for (Item item : itemsWithoutParents) {
            if (item.getIsItemDeleted()) {
                expandTreeChildren(item, rootTreeNode);
            }
        }

        return rootTreeNode;
    }
    
    @Override
    public String list() {
        return "deletedItemsList.xhtml?faces-redirect=true";
    }
    
    @Override
    public String viewForCurrentEntity() {
        return "viewDeletedItem?id=" + current.getId() + "&faces-redirect=true";
    }

    @Override
    protected void prepareEntityView(ItemDomainMachineDesign entity) {
        if ((entity != null) && (entity.getIsItemDeleted())) {
            loadViewModeUrlParameter();
            return;
        }
        super.prepareEntityView(entity);
    }

    @Override
    protected ItemDomainMachineDesign performItemRedirection(ItemDomainMachineDesign item, String paramString, boolean forceRedirection) {
        if ((item != null) && (item.getIsItemDeleted())) {
            setCurrent(item);
            prepareView(item);
            return item;
        }

        // Do default action. 
        return super.performItemRedirection(item, paramString, forceRedirection); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Returns value model for message in restore deleted item dialog.
     */
    public String getRestoreDeletedItemMessage() {
        return restoreDeletedItemMessage;
    }
    
    /**
     * Prepares dialog for restoring deleted item.
     */
    public void prepareRestoreDeletedItem() {
        
        updateCurrentUsingSelectedItemInTreeTable();
        ItemDomainMachineDesign itemToRestore = getCurrent();
        
        String itemType;
        if (itemToRestore.getIsItemTemplate()) {
            itemType = "machine template";
        } else if (itemToRestore.getIsItemInventory()) {
            itemType = "machine inventory";
        } else {
            itemType = "regular machine item";
        }
        
        restoreDeletedItemMessage = "Click 'Yes' to restore '" +
                itemToRestore.getName() +
                "' to " + itemType + " list. Click 'No' to cancel." +
                " NOTE: items restored from trash will appear as top-level items and not within their original container.";
                
    }
    
    /**
     * Executes action for restore deleted item dialog.
     */
    public void restoreDeletedItem() {
        
        ItemDomainMachineDesign itemToRestore = getCurrent();
        
        // collect list of items to restore
        List<ItemDomainMachineDesign> itemsToRestore = new ArrayList<>();
        List<ItemElement> elementsToRestore = new ArrayList<>();
        ValidInfo validInfo = collectItemsForDeletion(itemToRestore, itemsToRestore, elementsToRestore, true, true);
        if (!validInfo.isValid()) {
            SessionUtility.addErrorMessage("Error", "Could not restore: " + itemToRestore + " - " + validInfo.getValidString());
            return;
        }
        
        // check permissions for all items
        CdbRole sessionRole = (CdbRole) SessionUtility.getRole();
        UserInfo sessionUser = (UserInfo) SessionUtility.getUser();
        if (sessionRole != CdbRole.ADMIN) {
            for (ItemDomainMachineDesign item : itemsToRestore) {
                if (!AuthorizationUtility.isEntityWriteableByUser(item, sessionUser)) {
                    SessionUtility.addErrorMessage("Error", "Current user does not have permission to restore selected items");
                    return;
                }
            }
        }

        // remove 'deleted' entity type for all items
        for (ItemDomainMachineDesign item : itemsToRestore) {
            item.unsetIsDeleted();
        }
        
        if (itemsToRestore.size() == 1) {
            update();
        } else {
            try {
                updateList(itemsToRestore);
            } catch (CdbException ex) {
                // handled adequately by thrower
                return;
            }
        }
                
        // reset data models to refresh list views with changes (this
        // controller's models are reset by update/updateList())
        if (itemToRestore.getIsItemInventory()) {
            ItemDomainMachineDesignInventoryController.getInstance().resetListDataModel();
            ItemDomainMachineDesignInventoryController.getInstance().resetSelectDataModel();
        } else {
            ItemDomainMachineDesignController.getInstance().resetListDataModel();
            ItemDomainMachineDesignController.getInstance().resetSelectDataModel();
        }
    }

    public String getPermanentlyRemoveConfirmationName() {
        return permanentlyRemoveConfirmationName;
    }

    public void setPermanentlyRemoveConfirmationName(String permanentlyRemoveConfirmationName) {
        this.permanentlyRemoveConfirmationName = permanentlyRemoveConfirmationName;
    }
    
    public String getPermanentlyRemoveDisplayName() {
        return permanentlyRemoveDisplayName;
    }
    
    public String getPermanentlyRemoveMessage() {
        return permanentlyRemoveMessage;
    }
    
    public TreeNode getPermanentlyRemoveNode() {
        return permanentlyRemoveNode;
    }
    
    /**
     * Prepares dialog for permanently remove operation.
     */
    public void preparePermanentlyRemove() {
        updateCurrentUsingSelectedItemInTreeTable();
        permanentlyRemoveNode = null;
        permanentlyRemoveDisplayName = getCurrent().getName();
        permanentlyRemoveMessage = "'" + getCurrent().getName() + "'";
        if (!getCurrent().getItemElementDisplayList().isEmpty()) {
            permanentlyRemoveMessage = permanentlyRemoveMessage + 
                    " and its children (hierarchy shown at right)";
            permanentlyRemoveNode = new DefaultTreeNode();
            prepareItemNameHierarchyTree(permanentlyRemoveNode, getCurrent());
        }
    }
    
    /**
     * Executes permanently remove operation triggered by dialog 'Yes' button.
     */
    public void permanentlyRemove() {
        
        ItemDomainMachineDesign rootItemToDelete = getCurrent();
        if (rootItemToDelete == null) {
            return;
        }
        
        // check for match on item name entered by user in confirmation dialog
        if (!getPermanentlyRemoveConfirmationName().equals(rootItemToDelete.getName())) {
            SessionUtility.addErrorMessage("Error", "Item name entered by user: " + 
                            getPermanentlyRemoveConfirmationName() + 
                            " does not match selected item: " + 
                            rootItemToDelete.getName());
            return;
        }

        // collect list of items to delete
        List<ItemDomainMachineDesign> itemsToDelete = new ArrayList<>();
        List<ItemElement> elementsToDelete = new ArrayList<>();
        ValidInfo validInfo = collectItemsForDeletion(rootItemToDelete, itemsToDelete, elementsToDelete, true, false);
        if (!validInfo.isValid()) {
            SessionUtility.addErrorMessage("Error", "Could not delete: " + rootItemToDelete + " - " + validInfo.getValidString());
            return;
        }
        
        // admin permission required
        CdbRole sessionRole = (CdbRole) SessionUtility.getRole();
        if (sessionRole != CdbRole.ADMIN) {
            SessionUtility.addErrorMessage("Error", "Admin permission required to permanently delete item");
            return;
         }

        // mark ItemElements for relationships in hierarchy for deletion and
        // remove from parent and child items, and find container item
        ItemDomainMachineDesign containerItem = null;
        for (ItemElement ie : elementsToDelete) {
            Item childItem = ie.getContainedItem();
            Item ieParentItem = ie.getParentItem();
//            childItem.getItemElementMemberList().remove(ie);
            ieParentItem.removeItemElement(ie);
            ie.setMarkedForDeletion(true);
            if (childItem.equals(rootItemToDelete)) {
                containerItem = (ItemDomainMachineDesign)ieParentItem;
            }
        }        

        destroyList(itemsToDelete, containerItem);
        
        setPermanentlyRemoveConfirmationName(null);
        permanentlyRemoveNode = null;
    }
}
