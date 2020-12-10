/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.controllers;

import gov.anl.aps.cdb.common.exceptions.CdbException;
import gov.anl.aps.cdb.portal.constants.EntityTypeName;
import gov.anl.aps.cdb.portal.constants.ItemDomainName;
import gov.anl.aps.cdb.portal.controllers.extensions.ItemMultiEditController;
import gov.anl.aps.cdb.portal.controllers.extensions.ItemMultiEditDomainMachineDesignInventoryController;
import gov.anl.aps.cdb.portal.controllers.settings.ItemDomainMachineDesignInventorySettings;
import gov.anl.aps.cdb.portal.controllers.settings.ItemDomainMachineDesignSettings;
import gov.anl.aps.cdb.portal.import_export.import_.helpers.ImportHelperMachineInventory;
import gov.anl.aps.cdb.portal.model.db.entities.EntityType;
import gov.anl.aps.cdb.portal.model.db.entities.Item;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainInventory;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainMachineDesign;
import gov.anl.aps.cdb.portal.model.db.entities.ItemElement;
import gov.anl.aps.cdb.portal.model.db.entities.LocatableStatusItem;
import gov.anl.aps.cdb.portal.model.db.entities.PropertyType;
import gov.anl.aps.cdb.portal.model.db.entities.PropertyValue;
import gov.anl.aps.cdb.portal.model.db.entities.UserGroup;
import gov.anl.aps.cdb.portal.model.db.entities.UserInfo;
import gov.anl.aps.cdb.portal.model.db.utilities.ItemStatusUtility;
import gov.anl.aps.cdb.portal.utilities.SessionUtility;
import gov.anl.aps.cdb.portal.view.objects.DomainImportInfo;
import gov.anl.aps.cdb.portal.view.objects.ImportFormatInfo;
import gov.anl.aps.cdb.portal.view.objects.InventoryStatusPropertyTypeInfo;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;

@Named(ItemDomainMachineDesignInventoryController.controllerNamed)
@SessionScoped
public class ItemDomainMachineDesignInventoryController extends ItemDomainMachineDesignController implements IItemStatusController {

    public final static String controllerNamed = "itemDomainMachineDesignInventoryController";
    private static final Logger LOGGER = LogManager.getLogger(ItemDomainMachineDesignInventoryController.class.getName());
    
    private final static String pluginItemMachineDesignSectionsName = "itemMachineDesignInventoryDetailsViewSections";
    
    private InventoryStatusPropertyTypeInfo inventoryStatusPropertyTypeInfo = null;
    private PropertyType inventoryStatusPropertyType;

    private static ItemDomainMachineDesignInventoryController apiInstance;

    private ItemDomainMachineDesign newMdInventoryItem = null;

    @Override
    public void createListDataModel() {
        List<ItemDomainMachineDesign> itemList = getItemList();
        ListDataModel listDataModel = new ListDataModel(itemList);
        setListDataModel(listDataModel);
    }
    
    public boolean isCurrentTopLevel() {
        ItemDomainMachineDesign current = getCurrent();
        
        if (current != null) {
            List<ItemElement> itemElementMemberList = current.getItemElementMemberList();
            List<ItemElement> itemElementMemberList2 = current.getItemElementMemberList2();
            
            return itemElementMemberList.isEmpty() && itemElementMemberList2.isEmpty(); 
        }
        
        return false; 
    }

    public String getSubassemblyPageTitle() {
        String title = "Preassembled Machine: ";
        if (getCurrent() != null) {
            ItemDomainMachineDesign current = getCurrent();

            while (current.getParentMachineDesign() != null) {
                current = current.getParentMachineDesign();
            }

            title += current;
        }

        return title;
    } 

    @Override
    protected String getViewPath() {
        return "/views/itemDomainMachineDesignInventory/view.xhtml"; 
    }

    @Override
    public String getItemListPageTitle() {
        return "Preassembled Machine Elements (defined by Machine Templates)";
    }

    @Override
    public boolean getEntityDisplayDerivedFromItem() {
        return true;
    }

    @Override
    public String getDerivedFromItemTitle() {
        return "Machine Template";
    }

    @Override
    public boolean isDisplayRowExpansionForItem(Item item) {
        return super.isDisplayRowExpansionForItem(item); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDefaultDomainDerivedFromDomainName() {
        return ItemDomainName.machineDesign.getValue();
    }

    @Override
    public List<ItemDomainMachineDesign> getItemList() {
        return itemDomainMachineDesignFacade.getTopLevelMachineDesignInventory();
    } 

    @Override
    public DataModel getTopLevelMachineDesignSelectionList() {
        if (topLevelMachineDesignSelectionList == null) {
            List<ItemDomainMachineDesign> topLevelMachineDesignInventory = itemDomainMachineDesignFacade.getTopLevelMachineDesignInventory();
            
            removeTopLevelParentOfItemFromList(current, topLevelMachineDesignInventory);            
            
            topLevelMachineDesignSelectionList = new ListDataModel(topLevelMachineDesignInventory); 
        }
        
        return topLevelMachineDesignSelectionList;         
    }

    public static ItemDomainMachineDesignInventoryController getInstance() {
        if (SessionUtility.runningFaces()) {
            return (ItemDomainMachineDesignInventoryController) SessionUtility.findBean(controllerNamed);
        } else {
            return getApiInstance();
        }
    }

    public static synchronized ItemDomainMachineDesignInventoryController getApiInstance() {
        if (apiInstance == null) {
            apiInstance = new ItemDomainMachineDesignInventoryController();
            apiInstance.prepareApiInstance();
        }
        return apiInstance;
    }

    public void prepareCreateInventoryFromTemplate(ItemDomainMachineDesign template) {
        newMdInventoryItem = performPrepareCreateInventoryFromTemplate(template);
    }

    public ItemDomainMachineDesign performPrepareCreateInventoryFromTemplate(ItemDomainMachineDesign template) {
        return performPrepareCreateInventoryFromTemplate(template, null, null);
    }

    public ItemDomainMachineDesign performPrepareCreateInventoryFromTemplate(
            ItemDomainMachineDesign template,
            UserInfo ownerUser,
            UserGroup ownerGroup) {
        
        ItemDomainMachineDesign mdInventory = null;

        try {
            mdInventory = createItemFromTemplate(template, ownerUser, ownerGroup);
            createMachineDesignFromTemplateHierachically(mdInventory);
        } catch (CdbException | CloneNotSupportedException ex) {
            LOGGER.error(ex);
            SessionUtility.addErrorMessage("Error", ex.getMessage());
            return null;
        }

        List<Item> inventoryForCurrentTemplate = template.getDerivedFromItemList();
        int unitNum = inventoryForCurrentTemplate.size() + 1;
        mdInventory.setName(ItemDomainInventory.generatePaddedUnitName(unitNum));

        template.getDerivedFromItemList().add(mdInventory);

        assignInventoryAttributes(mdInventory, template);

        return mdInventory;
    }

    protected void assignInventoryAttributes(ItemDomainMachineDesign newInventory, ItemDomainMachineDesign templateItem) {
        newInventory.setDerivedFromItem(templateItem);
        assignInventoryAttributes(newInventory);
    }

    protected void assignInventoryAttributes(ItemDomainMachineDesign newInventory) {
        String inventoryetn = EntityTypeName.inventory.getValue();
        EntityType inventoryet = entityTypeFacade.findByName(inventoryetn);
        if (newInventory.getEntityTypeList() == null) {
            try {
                newInventory.setEntityTypeList(new ArrayList());
            } catch (CdbException ex) {
                LOGGER.error(ex);
            }
        }
        newInventory.getEntityTypeList().add(inventoryet);
        
        ItemStatusUtility.updateDefaultStatusProperty(newInventory, this);
    } 

    public void createInventoryFromTemplateSelected(NodeSelectEvent nodeSelection) {
        templateToCreateNewItemSelected(nodeSelection);
        prepareCreateInventoryFromTemplate(templateToCreateNewItem);
    }

    public void createInventoryFromDialog() {
        create();
    }

    @Override
    public String create() {
        ItemDomainMachineDesign newMdInventoryItem = getNewMdInventoryItem();
        if (newMdInventoryItem != null) {
            setCurrent(newMdInventoryItem);
            return super.create();
        } else {
            SessionUtility.addWarningMessage("No machine template selected", "Select machine template to continue.");
            return null;
        }
    }

    @Override
    public ItemDomainMachineDesign createEntityInstanceForDualTreeView() {
        ItemDomainMachineDesign item = super.createEntityInstanceForDualTreeView();

        assignInventoryAttributes(item);        

        return item;
    } 

    @Override
    public ItemDomainMachineDesign createMachineDesignFromTemplate(ItemElement itemElement, ItemDomainMachineDesign templateItem, UserInfo ownerUser, UserGroup ownerGroup) throws CdbException, CloneNotSupportedException {
        ItemDomainMachineDesign createItemFromTemplate = super.createMachineDesignFromTemplate(itemElement, templateItem, ownerUser, ownerGroup);
        
        assignInventoryAttributes(createItemFromTemplate, templateItem);

        return createItemFromTemplate;
    }

    @Override
    public TreeNode getCurrentMachineDesignListRootTreeNode() {
        return getMachineDesignFixtureRootTreeNode();
    }

    @Override
    protected void prepareEntityView(ItemDomainMachineDesign entity) {
        processPreRenderList();
        if (isMdInventory(entity)) {
            loadViewModeUrlParameter();
        }        
    }

    @Override
    public String currentDualViewList() {
        resetListConfigurationVariables();
        return view();
    }

    @Override
    protected ItemDomainMachineDesign performItemRedirection(ItemDomainMachineDesign item, String paramString, boolean forceRedirection) {
        if (isMdInventory(item)) {
            setCurrent(item);
            prepareView(item);
            resetListDataModel();
            return item;
        }

        // Do default action. 
        return super.performItemRedirection(item, paramString, forceRedirection); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected ItemDomainMachineDesignSettings createNewSettingObject() {
        return new ItemDomainMachineDesignInventorySettings(this);
    }

    private boolean isMdInventory(ItemDomainMachineDesign item) {
        if (item instanceof ItemDomainMachineDesign) {
            if (isInventory(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void resetVariablesForCurrent() {
        super.resetVariablesForCurrent();

        newMdInventoryItem = null;
    }

    @Override
    public ItemMultiEditController getItemMultiEditController() {
        return ItemMultiEditDomainMachineDesignInventoryController.getInstance();
    }

    public ItemDomainMachineDesign getNewMdInventoryItem() {
        return newMdInventoryItem;
    }
    
    @Override
    public String getPluginItemMachineDesignSectionsName() {
        return pluginItemMachineDesignSectionsName; 
    }

    @Override
    public void prepareEditInventoryStatus(LocatableStatusItem item) {
        ItemStatusUtility.prepareEditInventoryStatus(this, item);       
    }
        
    @Override
    public void prepareEditInventoryStatus(LocatableStatusItem item, UserInfo apiUser) {        
        ItemStatusUtility.prepareEditInventoryStatus(this, item, apiUser);
    }

    @Override
    public void prepareEditInventoryStatus() {
        ItemStatusUtility.prepareEditInventoryStatus(this);
    }

    @Override
    public String getStatusPropertyTypeName() {
        return ItemDomainMachineDesign.MD_INTERNAL_STATUS_PROPERTY_TYPE; 
    }

    @Override
    public PropertyValue getCurrentStatusPropertyValue() {
        return ItemStatusUtility.getCurrentStatusPropertyValue(this);
    }

    @Override
    public PropertyType getInventoryStatusPropertyType() {
        inventoryStatusPropertyType = ItemStatusUtility.getInventoryStatusPropertyType(this, propertyTypeFacade, inventoryStatusPropertyType);
        return inventoryStatusPropertyType;
    }

    @Override
    public InventoryStatusPropertyTypeInfo getInventoryStatusPropertyTypeInfo() {
        inventoryStatusPropertyTypeInfo = ItemStatusUtility.getInventoryStatusPropertyTypeInfo(this, inventoryStatusPropertyTypeInfo);
        return inventoryStatusPropertyTypeInfo;
    }

    @Override
    public InventoryStatusPropertyTypeInfo initializeInventoryStatusPropertyTypeInfo() {
        return ItemStatusUtility.initializeInventoryStatusPropertyTypeInfo(); 
    }

    @Override
    public boolean getRenderedHistoryButton() {
        return ItemStatusUtility.getRenderedHistoryButton(this);
    }
    
    public PropertyValue getItemStatusPropertyValue(LocatableStatusItem item) {
        return ItemStatusUtility.getItemStatusPropertyValue(item); 
    }

    @Override
    public boolean getEntityDisplayImportButton() {
        return true;
    }

    @Override
    protected DomainImportInfo initializeDomainImportInfo() {
        
        List<ImportFormatInfo> formatInfo = new ArrayList<>();
        formatInfo.add(new ImportFormatInfo("Basic Machine Inventory Format", ImportHelperMachineInventory.class));
        
        String completionUrl = "/views/itemDomainMachineDesignInventory/list?faces-redirect=true";
        
        return new DomainImportInfo(formatInfo, completionUrl);
    }

    public String deletedItemsList() {
        return "/views/itemDomainMachineDesign/deletedItemsList?faces-redirect=true";
    }
    
    /**
     * Executes move to trash operation invoked from confirmation dialog.
     * Invokes base implementation, and then redirects to the machine inventory
     * list view if the root item in the item view is moved to trash.
     */
    @Override
    public void moveToTrash() {
        ItemDomainMachineDesign item = getCurrent();
        boolean isTopLevelItem = (item.getParentMachineDesign() == null);
        ItemDomainMachineDesign rootItem = item;
        while (rootItem.getParentMachineDesign() != null) {
            rootItem = rootItem.getParentMachineDesign();
        }
        super.moveToTrash();
        if (isTopLevelItem && (item.getIsItemDeleted())) {
            // if we deleted root item in item view, redirect to machine inventory list view
            SessionUtility.navigateTo("list.xhtml?faces-redirect=true");
        } else {
            setCurrent(rootItem);
        }
    }
    
}
