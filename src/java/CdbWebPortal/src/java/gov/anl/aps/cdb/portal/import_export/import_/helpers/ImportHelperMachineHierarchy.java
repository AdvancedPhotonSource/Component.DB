/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.import_export.import_.helpers;

import gov.anl.aps.cdb.portal.controllers.ItemDomainLocationController;
import gov.anl.aps.cdb.portal.controllers.ItemDomainMachineDesignController;
import gov.anl.aps.cdb.portal.controllers.ItemProjectController;
import gov.anl.aps.cdb.portal.controllers.UserGroupController;
import gov.anl.aps.cdb.portal.controllers.UserInfoController;
import gov.anl.aps.cdb.portal.import_export.import_.objects.handlers.BooleanInputHandler;
import gov.anl.aps.cdb.portal.import_export.import_.objects.specs.ColumnSpec;
import gov.anl.aps.cdb.portal.import_export.import_.objects.CreateInfo;
import gov.anl.aps.cdb.portal.import_export.import_.objects.handlers.HierarchyHandler;
import gov.anl.aps.cdb.portal.import_export.import_.objects.ImportInfo;
import gov.anl.aps.cdb.portal.import_export.import_.objects.InputColumnInfo;
import gov.anl.aps.cdb.portal.import_export.import_.objects.InputColumnModel;
import gov.anl.aps.cdb.portal.import_export.import_.objects.handlers.InputHandler;
import gov.anl.aps.cdb.portal.import_export.import_.objects.OutputColumnModel;
import gov.anl.aps.cdb.portal.import_export.import_.objects.handlers.SingleColumnInputHandler;
import gov.anl.aps.cdb.portal.import_export.import_.objects.handlers.StringInputHandler;
import gov.anl.aps.cdb.portal.import_export.import_.objects.ValidInfo;
import gov.anl.aps.cdb.portal.import_export.import_.objects.handlers.RefInputHandler;
import gov.anl.aps.cdb.portal.model.db.beans.ItemFacade;
import gov.anl.aps.cdb.portal.model.db.entities.Item;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainCatalog;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainInventory;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainLocation;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainMachineDesign;
import gov.anl.aps.cdb.portal.model.db.entities.ItemElement;
import gov.anl.aps.cdb.portal.model.db.entities.UserGroup;
import gov.anl.aps.cdb.portal.model.db.entities.UserInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;

/**
 *
 * @author craig
 */
public class ImportHelperMachineHierarchy extends HierarchicalImportHelperBase<ItemDomainMachineDesign, ItemDomainMachineDesignController> {
    
    /**
     * Using a custom handler so that we can use catalog or inventory item id's
     * in a single column.  There is not a way to do this with IdRef handler, as
     * it needs a particular controller instance to use for the lookup.  The
     * query we need here is on the ItemFacade.
     */
    private class AssignedItemHandler extends SingleColumnInputHandler {
        
        public AssignedItemHandler(int columnIndex) {
            super(columnIndex, HEADER_ASSIGNED_ITEM);
        }
        
        @Override
        public ValidInfo handleInput(
                Row row,
                Map<Integer, String> cellValueMap,
                Map<String, Object> rowMap) {
            
            boolean isValid = true;
            String validString = "";
            
            String parsedValue = cellValueMap.get(columnIndex);
            
            Item assignedItem = null;
            if ((parsedValue != null) && (!parsedValue.isEmpty())) {
                // assigned item is specified
                
                int id;
                try {
                    id = Integer.valueOf(parsedValue);
                    assignedItem = ItemFacade.getInstance().findById(id);
                    if (assignedItem == null) {
                        String msg = "Unable to find object for: " + getColumnName()
                                + " with id: " + parsedValue;
                        isValid = false;
                        validString = msg;
                        LOGGER.info("AssignedItemHandler.handleInput() " + msg);
                    }
                    rowMap.put(KEY_ASSIGNED_ITEM, assignedItem);
                    
                } catch (NumberFormatException ex) {
                    String msg = "Invalid id number: " + parsedValue + " for column: " + getColumnName();
                    isValid = false;
                    validString = msg;
                    LOGGER.info("AssignedItemHandler.handleInput() " + msg);  
                }                
            }

            return new ValidInfo(isValid, validString);
        }
    }
    
    /**
     * Using a custom handler for location so we can ignore the word "parent"
     * in a column that otherwise expects location item id's.  We could use the
     * standard IdRef handler if we didn't need to worry about "parent".
     */
    private class LocationHandler extends SingleColumnInputHandler {
        
        public LocationHandler(int columnIndex) {
            super(columnIndex, HEADER_LOCATION);
        }
        
        @Override
        public ValidInfo handleInput(
                Row row,
                Map<Integer, String> cellValueMap,
                Map<String, Object> rowMap) {
            
            boolean isValid = true;
            String validString = "";
            
            String parsedValue = cellValueMap.get(columnIndex);
            
            ItemDomainLocation itemLocation = null;
            if ((parsedValue != null) && (!parsedValue.isEmpty())) {
                // location is specified
                
                // ignore word "parent"
                if (!parsedValue.equalsIgnoreCase("parent")) {
                    int id;
                    try {
                        id = Integer.valueOf(parsedValue);
                        itemLocation = ItemDomainLocationController.getInstance().findById(id);
                        if (itemLocation == null) {
                            String msg = "Unable to find object for: " + getColumnName()
                                    + " with id: " + parsedValue;
                            isValid = false;
                            validString = msg;
                            LOGGER.info("LocationHandler.handleInput() " + msg);

                        } else {
                            // set location
                            rowMap.put(KEY_LOCATION, itemLocation);
                        }

                    } catch (NumberFormatException ex) {
                        String msg = "Invalid id number: " + parsedValue + " for column: " + getColumnName();
                        isValid = false;
                        validString = msg;
                        LOGGER.info("LocationHandler.handleInput() " + msg);
                    }              
                }
            }

            return new ValidInfo(isValid, validString);
        }
    }
    
    private static final Logger LOGGER = LogManager.getLogger(ImportHelperMachineHierarchy.class.getName());
    
    private static final String KEY_NAME = "name";
    private static final String KEY_INDENT = "indentLevel";
    private static final String KEY_ASSIGNED_ITEM = "assignedItem";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_CONTAINER = "importMdItem";
    private static final String KEY_IS_TEMPLATE = "importIsTemplate";
    
    private static final String HEADER_PARENT = "Parent ID";
    private static final String HEADER_BASE_LEVEL = "Level";
    private static final String HEADER_ALT_NAME = "Machine Design Alternate Name";
    private static final String HEADER_DESCRIPTION = "Machine Design Item Description";
    private static final String HEADER_ASSIGNED_ITEM = "Assigned Catalog/Inventory Item";
    private static final String HEADER_ASSIGNED_ITEM_ID = "Assigned Catalog/Inventory Item ID";
    private static final String HEADER_LOCATION = "Location";
    private static final String HEADER_LOCATION_DETAILS = "Location Details";
    private static final String HEADER_PROJECT = "Project ID";
    private static final String HEADER_TEMPLATE = "Is Template?";
    private static final String HEADER_USER = "Owner User";
    private static final String HEADER_GROUP = "Owner Group";

    private Map<String, InputColumnInfo> columnInfoMap = null;
    private Map<String, ItemDomainMachineDesign> itemByNameMap = new HashMap<>();
    private Map<ItemDomainMachineDesign, ImportInfo> itemInfoMap = new HashMap<>();
    private Map<Integer, ItemDomainMachineDesign> parentIndentMap = new HashMap<>();
    
    private int nonTemplateItemCount = 0;
    private int templateItemCount = 0;
    
    private void initColumnInfoMap() {
        
        columnInfoMap = new HashMap<>();
        
        columnInfoMap.put(HEADER_PARENT, new InputColumnInfo(
                HEADER_PARENT, 
                false, 
                "CDB ID or name of parent machine design item.  Can only be provided for level 0 item. Name must be unique and prefixed with '#'."));
        
        columnInfoMap.put(HEADER_BASE_LEVEL, new InputColumnInfo(
                HEADER_BASE_LEVEL, 
                false, 
                "Machine design hierarchy column level"));
        
        columnInfoMap.put(HEADER_ALT_NAME, new InputColumnInfo(
                HEADER_ALT_NAME, 
                false, 
                "Alternate machine design item name."));
        
        columnInfoMap.put(HEADER_DESCRIPTION, new InputColumnInfo(
                HEADER_DESCRIPTION, 
                false, 
                "Textual description of machine design item."));
        
        columnInfoMap.put(HEADER_ASSIGNED_ITEM, new InputColumnInfo(
                HEADER_ASSIGNED_ITEM, 
                false, 
                "Name of assigned catalog or inventory item (optional, for reference only)."));
        
        columnInfoMap.put(HEADER_ASSIGNED_ITEM_ID, new InputColumnInfo(
                HEADER_ASSIGNED_ITEM_ID, 
                false, 
                "CDB ID or name of assigned catalog or inventory item. Name must be unique and prefixed with '#'."));
        
        columnInfoMap.put(HEADER_LOCATION, new InputColumnInfo(
                HEADER_LOCATION, 
                false, 
                "CDB ID or name of CDB location item (use of word 'parent' allowed for documentation purposes, it is ignored). Name must be unique and prefixed with '#'."));
        
        columnInfoMap.put(HEADER_LOCATION_DETAILS, new InputColumnInfo(
                HEADER_LOCATION_DETAILS, 
                false, 
                "Location details (text)."));
        
        columnInfoMap.put(HEADER_TEMPLATE, new InputColumnInfo(
                HEADER_TEMPLATE, 
                true, 
                "TRUE if item is template, false otherwise."));
        
        columnInfoMap.put(HEADER_PROJECT, new InputColumnInfo(
                HEADER_PROJECT, 
                true, 
                "Comma-separated list of IDs of CDB project(s). Name must be unique and prefixed with '#'."));
        
        columnInfoMap.put(HEADER_USER, new InputColumnInfo(
                HEADER_USER, 
                false, 
                "CDB ID or name of owner user. Name must be unique and prefixed with '#'."));
        
        columnInfoMap.put(HEADER_GROUP, new InputColumnInfo(
                HEADER_GROUP, 
                false, 
                "CDB ID or name of owner group. Name must be unique and prefixed with '#'."));
        
    }
    
    private Map<String, InputColumnInfo> getColumnInfoMap() {
        if (columnInfoMap == null) {
            initColumnInfoMap();
        }
        return columnInfoMap;
    }
    
    /**
     * Returns list of columns for download template file feature.  Because
     * initialize_() creates the columns dynamically after the spreadsheet file
     * is opened and read, the sample list of columns is hardwired here.
     * The number of "Level" columns is variable, and depends on the levels of
     * hierarchy to be added for a particular import.  Updates to input columns
     * must unfortunately be made in two places, here and in initialize_().
     */
    @Override
    protected List<InputColumnModel> getTemplateColumns() {
        
        List<InputColumnModel> inputColumns = new ArrayList<>();
        
        InputColumnInfo colInfo = getColumnInfoMap().get(HEADER_PARENT);
        inputColumns.add(new InputColumnModel(0, HEADER_PARENT, colInfo.isRequired, colInfo.description));
        
        colInfo = getColumnInfoMap().get(HEADER_BASE_LEVEL);
        inputColumns.add(new InputColumnModel(1, HEADER_BASE_LEVEL + " 0", true, colInfo.description + " 0"));
        inputColumns.add(new InputColumnModel(2, HEADER_BASE_LEVEL + " 1", false, colInfo.description + " 1"));
        inputColumns.add(new InputColumnModel(3, HEADER_BASE_LEVEL + " 2", false, colInfo.description + " 2"));
        
        colInfo = getColumnInfoMap().get(HEADER_ALT_NAME);
        inputColumns.add(new InputColumnModel(4, HEADER_ALT_NAME, colInfo.isRequired, colInfo.description));
        
        colInfo = getColumnInfoMap().get(HEADER_DESCRIPTION);
        inputColumns.add(new InputColumnModel(5, HEADER_DESCRIPTION, colInfo.isRequired, colInfo.description));

        colInfo = getColumnInfoMap().get(HEADER_ASSIGNED_ITEM);
        inputColumns.add(new InputColumnModel(6, HEADER_ASSIGNED_ITEM, colInfo.isRequired, colInfo.description));

        colInfo = getColumnInfoMap().get(HEADER_ASSIGNED_ITEM_ID);
        inputColumns.add(new InputColumnModel(7, HEADER_ASSIGNED_ITEM_ID, colInfo.isRequired, colInfo.description));

        colInfo = getColumnInfoMap().get(HEADER_LOCATION);
        inputColumns.add(new InputColumnModel(8, HEADER_LOCATION, colInfo.isRequired, colInfo.description));

        colInfo = getColumnInfoMap().get(HEADER_LOCATION_DETAILS);
        inputColumns.add(new InputColumnModel(9, HEADER_LOCATION_DETAILS, colInfo.isRequired, colInfo.description));

        colInfo = getColumnInfoMap().get(HEADER_TEMPLATE);
        inputColumns.add(new InputColumnModel(10, HEADER_TEMPLATE, colInfo.isRequired, colInfo.description));

        colInfo = getColumnInfoMap().get(HEADER_PROJECT);
        inputColumns.add(new InputColumnModel(11, HEADER_PROJECT, colInfo.isRequired, colInfo.description));

        colInfo = getColumnInfoMap().get(HEADER_USER);
        inputColumns.add(new InputColumnModel(12, HEADER_USER, colInfo.isRequired, colInfo.description));

        colInfo = getColumnInfoMap().get(HEADER_GROUP);
        inputColumns.add(new InputColumnModel(13, HEADER_GROUP, colInfo.isRequired, colInfo.description));

        return inputColumns;
    }
    
    @Override
    protected List<ColumnSpec> getColumnSpecs() {
        return new ArrayList<>();
    }
    
    /**
     * Builds the input columns, handlers, and output columns for the helper
     * framework.  Note that because the number of "Level" columns is variable
     * the column layout is not fixed as is the case for some of the other
     * helpers.  Changes to input columns, therefore, must be made in two places,
     * here and in getTemplateColumns().
     */
    @Override
    protected ValidInfo initialize_(
            int actualColumnCount,
            Map<Integer, String> headerValueMap,
            List<InputColumnModel> inputColumns,
            List<InputHandler> inputHandlers,
            List<OutputColumnModel> outputColumns) {
        
        String methodLogName = "initialize_() ";

        boolean isValid = true;
        String validString = "";
        
        boolean foundLevel = false;
        int firstLevelIndex = -1;
        int lastLevelIndex = -1;
        
        InputColumnInfo colInfo;
        
        for (Entry<Integer, String> entry : headerValueMap.entrySet()) {
            
            int columnIndex = entry.getKey();
            String columnHeader = entry.getValue();
            
            // check to see if this is a "level" column
            if (columnHeader.startsWith(HEADER_BASE_LEVEL)) {
                colInfo = getColumnInfoMap().get(HEADER_BASE_LEVEL);
                inputColumns.add(new InputColumnModel(columnIndex, columnHeader, false, colInfo.description));
                foundLevel = true;
                if (firstLevelIndex == -1) {
                    firstLevelIndex = columnIndex;
                }
                lastLevelIndex = columnIndex;
                
            } else {
            
                switch (columnHeader) {

                    case HEADER_PARENT:
                        colInfo = getColumnInfoMap().get(HEADER_PARENT);
                        inputColumns.add(new InputColumnModel(columnIndex, columnHeader, colInfo.isRequired, colInfo.description));
                        inputHandlers.add(new RefInputHandler(columnIndex, HEADER_PARENT, KEY_CONTAINER, "setImportMdItem", ItemDomainMachineDesignController.getInstance(), ItemDomainMachineDesign.class, "", false, true));
                        break;

                    case HEADER_ALT_NAME:
                        colInfo = getColumnInfoMap().get(HEADER_ALT_NAME);
                        inputColumns.add(new InputColumnModel(columnIndex, columnHeader, colInfo.isRequired, colInfo.description));
                        inputHandlers.add(new StringInputHandler(columnIndex, HEADER_ALT_NAME, "alternateName", "setAlternateName", 128));
                        break;

                    case HEADER_DESCRIPTION:
                        colInfo = getColumnInfoMap().get(HEADER_DESCRIPTION);
                        inputColumns.add(new InputColumnModel(columnIndex, columnHeader, colInfo.isRequired, colInfo.description));
                        inputHandlers.add(new StringInputHandler(columnIndex, HEADER_DESCRIPTION, "description", "setDescription", 256));
                        break;

                    case HEADER_ASSIGNED_ITEM:
                        colInfo = getColumnInfoMap().get(HEADER_ASSIGNED_ITEM);
                        inputColumns.add(new InputColumnModel(columnIndex, columnHeader, colInfo.isRequired, colInfo.description));
                        break;

                    case HEADER_ASSIGNED_ITEM_ID:
                        colInfo = getColumnInfoMap().get(HEADER_ASSIGNED_ITEM_ID);
                        inputColumns.add(new InputColumnModel(columnIndex, columnHeader, colInfo.isRequired, colInfo.description));
                        inputHandlers.add(new AssignedItemHandler(columnIndex));
                        break;

                    case HEADER_LOCATION:
                        colInfo = getColumnInfoMap().get(HEADER_LOCATION);
                        inputColumns.add(new InputColumnModel(columnIndex, columnHeader, colInfo.isRequired, colInfo.description));
                        inputHandlers.add(new LocationHandler(columnIndex));
                        break;

                    case HEADER_LOCATION_DETAILS:
                        colInfo = getColumnInfoMap().get(HEADER_LOCATION_DETAILS);
                        inputColumns.add(new InputColumnModel(columnIndex, columnHeader, colInfo.isRequired, colInfo.description));
                        inputHandlers.add(new StringInputHandler(columnIndex, HEADER_LOCATION_DETAILS, "locationDetails", "setLocationDetails", 256));
                        break;

                    case HEADER_TEMPLATE:
                        colInfo = getColumnInfoMap().get(HEADER_TEMPLATE);
                        inputColumns.add(new InputColumnModel(columnIndex, columnHeader, colInfo.isRequired, colInfo.description));
                        inputHandlers.add(new BooleanInputHandler(columnIndex, HEADER_TEMPLATE, KEY_IS_TEMPLATE, "setImportIsTemplate"));
                        break;

                    case HEADER_PROJECT:
                        colInfo = getColumnInfoMap().get(HEADER_PROJECT);
                        inputColumns.add(new InputColumnModel(columnIndex, columnHeader, colInfo.isRequired, colInfo.description));
                        inputHandlers.add(new RefInputHandler(columnIndex, HEADER_PROJECT, "itemProjectString", "setItemProjectList", ItemProjectController.getInstance(), List.class, "", false, false));
                        break;

                    case HEADER_USER:
                        colInfo = getColumnInfoMap().get(HEADER_USER);
                        inputColumns.add(new InputColumnModel(columnIndex, columnHeader, colInfo.isRequired, colInfo.description));
                        inputHandlers.add(new RefInputHandler(columnIndex, HEADER_USER, "ownerUserName", "setOwnerUser", UserInfoController.getInstance(), UserInfo.class, "", false, true));
                        break;

                    case HEADER_GROUP:
                        colInfo = getColumnInfoMap().get(HEADER_GROUP);
                        inputColumns.add(new InputColumnModel(columnIndex, columnHeader, colInfo.isRequired, colInfo.description));
                        inputHandlers.add(new RefInputHandler(columnIndex, HEADER_GROUP, "ownerUserGroupName", "setOwnerUserGroup", UserGroupController.getInstance(), UserGroup.class, "", false, true));
                        break;

                    default:
                        // unexpected column found, so fail
                        isValid = false;
                        String msg = "found unexpected column header: " + columnHeader;
                        validString = msg;
                        LOGGER.info(methodLogName + msg);
                }
            }
        }
        
        if (!foundLevel) {
            // didn't find any "Level" columns, so fail
            isValid = false;
            String msg = "one or more 'Level' columns is required";
            validString = msg;
            LOGGER.info(methodLogName + msg);
            
        } else {
            // add handler for multiple "level" columns
            inputHandlers.add(new HierarchyHandler(
                    firstLevelIndex, lastLevelIndex, 128, KEY_NAME, KEY_INDENT));
        }
        
        // output columns are fixed
        outputColumns.add(new OutputColumnModel(0, "Parent Item", "importContainerString"));
        outputColumns.add(new OutputColumnModel(1, "Parent Path", "importPath"));
        outputColumns.add(new OutputColumnModel(2, "Name", "name"));
        outputColumns.add(new OutputColumnModel(3, "Is Template", "importIsTemplateString"));
        outputColumns.add(new OutputColumnModel(4, "Alt Name", "alternateName"));
        outputColumns.add(new OutputColumnModel(5, "Description", "description"));
        outputColumns.add(new OutputColumnModel(6, "Assigned Catalog Item", "importAssignedCatalogItemString"));
        outputColumns.add(new OutputColumnModel(7, "Assigned Inventory Item", "importAssignedInventoryItemString"));
        outputColumns.add(new OutputColumnModel(8, "Location", "importLocationItemString"));
        outputColumns.add(new OutputColumnModel(9, "Location Details", "locationDetails"));
        outputColumns.add(new OutputColumnModel(10, "Project", "itemProjectString"));
        outputColumns.add(new OutputColumnModel(11, "Owner User", "ownerUserName"));
        outputColumns.add(new OutputColumnModel(12, "Owner Group", "ownerUserGroupName"));
        
        return new ValidInfo(isValid, validString);
    }
    
    @Override
    public ItemDomainMachineDesignController getEntityController() {
        return ItemDomainMachineDesignController.getInstance();
    }

    @Override
    public String getTemplateFilename() {
        return "Machine Hierarchy Template";
    }
    
    @Override
    protected void reset_() {
        itemByNameMap = new HashMap<>();
        columnInfoMap = null;
        itemInfoMap = new HashMap<>();
        parentIndentMap = new HashMap<>();
        nonTemplateItemCount = 0;
        templateItemCount = 0;
    }
    
    @Override
    protected ItemDomainMachineDesign getItemParent(ItemDomainMachineDesign item) {
        return null;
    }
    
    @Override
    protected String getItemName(ItemDomainMachineDesign item) {
        return item.getName();
    }
    
    @Override
    protected List<ItemDomainMachineDesign> getItemChildren(ItemDomainMachineDesign item) {
        List<ItemElement> children = item.getItemElementDisplayList();
        return children.stream()
                .map((child) -> (ItemDomainMachineDesign) child.getContainedItem())
                .collect(Collectors.toList());
    }
            
    @Override
    protected CreateInfo createEntityInstance(Map<String, Object> rowMap) {
        
        String methodLogName = "createEntityForRegularItem() ";
        boolean isValid = true;
        String validString = "";

        boolean isValidAssignedItem = true;

        ItemDomainMachineDesign item = null;
        ItemDomainMachineDesign itemParent = null;

        item = getEntityController().createEntityInstance();

        item.setName((String) rowMap.get(KEY_NAME));
        if ((item.getName() == null) || (item.getName().isEmpty())) {
            // didn't find a non-empty name column for this row
            isValid = false;
            validString = "name columns are all empty";
            LOGGER.info(methodLogName + validString);
            return new CreateInfo(item, isValid, validString);
        }

        // set flag indicating item is template
        Boolean itemIsTemplate = (Boolean) rowMap.get(KEY_IS_TEMPLATE);
        if (itemIsTemplate != null) {
            item.setImportIsTemplate(itemIsTemplate);
        } else {
            // return because we need this value to continue
            isValid = false;
            validString = ""; // we don't need a message because this is already flagged as invalid because it is a required column"
            LOGGER.info(methodLogName + validString);
            return new CreateInfo(item, isValid, validString);
        }

        // set assigned item
        Item assignedItem = (Item) rowMap.get(KEY_ASSIGNED_ITEM);
        if (assignedItem != null) {
            if (assignedItem instanceof ItemDomainCatalog) {
                item.setImportAssignedCatalogItem((ItemDomainCatalog) assignedItem);
            } else if (assignedItem instanceof ItemDomainInventory) {
                item.setImportAssignedInventoryItem((ItemDomainInventory) assignedItem);
            } else {
                String msg = "Invalid object type for assigned item: " + assignedItem.getClass().getName();
                isValid = false;
                validString = msg;
                LOGGER.info(methodLogName + msg);
            }
        }

        // set location
        ItemDomainLocation itemLocation = (ItemDomainLocation) rowMap.get(KEY_LOCATION);
        if (itemLocation != null) {
            item.setImportLocationItem(itemLocation);
        }

        // find parent for this item
        
        if (!rowMap.containsKey(KEY_INDENT)) {
            // return because we need this value to continue
            isValid = false;
            validString = "missing indent level map entry";
            LOGGER.info(methodLogName + validString);
            return new CreateInfo(item, isValid, validString);
        }        
        int itemIndentLevel = (int) rowMap.get(KEY_INDENT);
        
        ItemDomainMachineDesign itemContainer
                = (ItemDomainMachineDesign) rowMap.get(KEY_CONTAINER);
        
        if (itemIndentLevel > 1) {

            // not allowed to specify parent for non level 0 item
            if (itemContainer != null) {
                String msg = "Can only specify existing parent for level 0 item";
                LOGGER.info(methodLogName + msg);
                validString = appendToString(validString, msg);
                isValid = false;
            }

            // find parent at previous indent level
            itemParent = parentIndentMap.get(itemIndentLevel - 1);

            if (itemParent == null) {
                // should have a parent for this item in map
                String msg = "Unable to determine parent for item";
                LOGGER.info(methodLogName + msg);
                validString = appendToString(validString, msg);
                isValid = false;
            }

            // create "parent path" to display item hierarchy in validation table
            String path = "";
            for (int i = 1; i < itemIndentLevel; i++) {
                ItemDomainMachineDesign pathItem = parentIndentMap.get(i);
                path = path + pathItem.getName() + "/ ";
            }
            item.setImportPath(path);

        } else {
            // this is either a top-level item, or a parent item is explicitly specified for it
            if (itemContainer == null) {
                // new item is a top-level machine design with no parent
                String msg = "creating top-level machine design item: " + item.getName();
                LOGGER.debug(methodLogName + msg);
                itemParent = null;
            } else {
                itemParent = itemContainer;
            }
        }

        if (item.getIsItemTemplate()) {
            // template item handling
            
            templateItemCount = templateItemCount + 1;

            if ((item.getImportAssignedInventoryItem() != null)) {

                // template not allowed to have assigned inventory
                String msg = "Template cannot have assigned inventory item";
                LOGGER.info(methodLogName + msg);
                validString = appendToString(validString, msg);
                isValid = false;
                isValidAssignedItem = false;
            }

            if (item.getImportLocationItem() != null) {
                // template not allowed to have location
                String msg = "Template cannot have location item";
                LOGGER.info(methodLogName + msg);
                validString = appendToString(validString, msg);
                isValid = false;
            }

        } else {
            
            // non-template item handling
            nonTemplateItemCount = nonTemplateItemCount + 1;

        }

        if (itemParent != null) {
            // hanlding for all items with parent, template or non-template
            if (!Objects.equals(item.getIsItemTemplate(), itemParent.getIsItemTemplate())) {
                // parent and child must both be templates or both not be
                String msg = "parent and child must both be templates or both not be templates";
                LOGGER.info(methodLogName + msg);
                validString = appendToString(validString, msg);
                isValid = false;
            }            
            item.setImportChildParentRelationship(itemParent);
        }
        
        if (isValidAssignedItem) {
            item.applyImportAssignedItem();
        }

        // set current item as last parent at its indent level
        parentIndentMap.put(itemIndentLevel, item);

        // update tree view with item and parent
        updateTreeView(item, itemParent, false);

        // add entry to name map for new item
        itemByNameMap.put(item.getName(), item);

        return new CreateInfo(item, isValid, validString);
    }

    protected String getCustomSummaryDetails() {
        
        String summaryDetails = "";
        
        if (templateItemCount > 0) {
            String templateItemDetails = templateItemCount + " template items";
            if (summaryDetails.isEmpty()) {
                summaryDetails = templateItemDetails;                        
            } else {
                summaryDetails = summaryDetails + ", " + templateItemDetails;
            }
        }
        
        if (nonTemplateItemCount > 0) {
            String nontemplateItemDetails = nonTemplateItemCount + " regular items";
            if (summaryDetails.isEmpty()) {
                summaryDetails = nontemplateItemDetails;
            } else {
                summaryDetails = summaryDetails + ", " + nontemplateItemDetails;
            }
        }
        
        return summaryDetails;
    }
}