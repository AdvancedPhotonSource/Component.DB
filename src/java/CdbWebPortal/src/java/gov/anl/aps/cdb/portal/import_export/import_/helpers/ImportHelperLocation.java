/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.import_export.import_.helpers;

import gov.anl.aps.cdb.portal.constants.ItemDomainName;
import gov.anl.aps.cdb.portal.controllers.ItemDomainLocationController;
import gov.anl.aps.cdb.portal.controllers.ItemTypeController;
import gov.anl.aps.cdb.portal.import_export.import_.objects.ValidInfo;
import gov.anl.aps.cdb.portal.import_export.import_.objects.specs.ColumnSpec;
import gov.anl.aps.cdb.portal.import_export.import_.objects.specs.IdOrNameRefColumnSpec;
import gov.anl.aps.cdb.portal.import_export.import_.objects.specs.IntegerColumnSpec;
import gov.anl.aps.cdb.portal.import_export.import_.objects.specs.NameHierarchyColumnSpec;
import gov.anl.aps.cdb.portal.import_export.import_.objects.specs.StringColumnSpec;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainLocation;
import gov.anl.aps.cdb.portal.model.db.entities.ItemElement;
import gov.anl.aps.cdb.portal.model.db.entities.ItemType;
import gov.anl.aps.cdb.portal.model.db.entities.UserGroup;
import gov.anl.aps.cdb.portal.model.db.entities.UserInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author craig
 */
public class ImportHelperLocation 
        extends ImportHelperHierarchyBase<ItemDomainLocation, ItemDomainLocationController> {
    
    private static final String KEY_NAME = "name";
    private static final String KEY_INDENT = "indentLevel";
    private static final String KEY_PARENT = "importParentItem";
    private static final String KEY_QR = "qrId";
    private static final String KEY_TYPE = "itemTypeString";
    private static final String KEY_SORT_ORDER = "importSortOrder";
    
    private static final String HEADER_PARENT = "Parent Location ID";
    private static final String HEADER_BASE_LEVEL = "Level";
    private static final String HEADER_QR = "QR ID";
    private static final String HEADER_TYPE = "Type ID";
    private static final String HEADER_DESCRIPTION = "Description";
    private static final String HEADER_SORT_ORDER = "Sort Order";
    
    private int itemCount = 0;
    
    @Override
    protected List<ColumnSpec> getColumnSpecs() {
        List<ColumnSpec> specs = new ArrayList<>();
        
        specs.add(new IdOrNameRefColumnSpec(HEADER_PARENT, KEY_PARENT, "setImportParentItem", false, "CDB ID or name of parent location item.  Can only be provided for level 0 item. Name must be unique and prefixed with '#'.", ItemDomainLocationController.getInstance(), ItemDomainLocation.class, ""));
        specs.add(new NameHierarchyColumnSpec(HEADER_BASE_LEVEL, KEY_NAME, KEY_INDENT, "Name hierarchy column", 3));
        specs.add(new IntegerColumnSpec(HEADER_QR, KEY_QR, "setQrId", false, "QR ID of location (9 digit number)."));
        specs.add(new IdOrNameRefColumnSpec(HEADER_TYPE, KEY_TYPE, "setItemType", false, "CDB ID or name of location type. Name must be prefixed with '#'.", ItemTypeController.getInstance(), ItemType.class, ItemDomainName.location.getValue()));
        specs.add(new StringColumnSpec(HEADER_DESCRIPTION, "description", "setDescription", false, "Textual description of location.", 256));
        specs.add(new IntegerColumnSpec(HEADER_SORT_ORDER, KEY_SORT_ORDER, "setImportSortOrder", false, "Sort order within parent item."));
        specs.add(ownerUserColumnSpec());
        specs.add(ownerGroupColumnSpec());
        
        return specs;
    }
    
    @Override
    public ItemDomainLocationController getEntityController() {
        return ItemDomainLocationController.getInstance();
    }

    @Override
    public String getTemplateFilename() {
        return "Location Template";
    }
    
    @Override
    protected void reset_() {
        super.reset_();
        itemCount = 0;
    }
    
    @Override
    protected ItemDomainLocation getItemParent(ItemDomainLocation item) {
        return null;
    }
    
    @Override
    protected String getItemName(ItemDomainLocation item) {
        return item.getName();
    }
    
    @Override
    protected List<ItemDomainLocation> getItemChildren(ItemDomainLocation item) {
        List<ItemElement> children = item.getItemElementDisplayList();
        return children.stream()
                .map((child) -> (ItemDomainLocation) child.getContainedItem())
                .collect(Collectors.toList());
    }
    
    @Override
    protected ItemDomainLocation newInstance_() {
        return getEntityController().createEntityInstance();
    }
    
    @Override
    protected String getKeyName_() {
        return KEY_NAME;
    }
            
    @Override
    protected String getKeyIndent_() {
        return KEY_INDENT;
    }
            
    @Override
    protected String getKeyParent_() {
        return KEY_PARENT;
    }
            
    @Override
    protected String getKeySortOrder_() {
        return KEY_SORT_ORDER;
    }
            
    @Override
    protected ValidInfo initEntityInstance_(
            ItemDomainLocation item,
            ItemDomainLocation itemParent,
            Map<String, Object> rowMap,
            String itemName,
            String itemPath,
            Integer itemSortOrderInt) {
        
        boolean isValid = true;
        String validString = "";

        item.setName(itemName);
        item.setImportPath(itemPath);
        item.setImportSortOrder(itemSortOrderInt);
        itemCount = itemCount + 1;

        if (itemParent != null) {
            Float itemSortOrderFloat = itemSortOrderInt.floatValue();
            String childItemElementName = String.valueOf(itemSortOrderInt);
            
            // validate name and sort order are unique for specified parent item
            List<ItemElement> ieList = itemParent.getFullItemElementList();
            for (ItemElement ie : ieList) {
                if ((ie.getSortOrder() != null) && (ie.getSortOrder().equals(itemSortOrderFloat))) {
                    String msg = "duplicate sort order for existing child location";
                    validString = appendToString(validString, msg);
                    isValid = false;
                }
                if ((ie.getName() != null) && (ie.getName().equals(childItemElementName))) {
                    String msg = "duplicate item element name for existing child location";
                    validString = appendToString(validString, msg);
                    isValid = false;
                }
            }
            
            UserInfo user = (UserInfo) rowMap.get(KEY_USER);
            UserGroup group = (UserGroup) rowMap.get(KEY_GROUP);
            item.setImportChildParentRelationship(
                    itemParent, childItemElementName, itemSortOrderFloat, user, group);
        }
        
        return new ValidInfo(isValid, validString);
    }

    protected String getCustomSummaryDetails() {        
        String summaryDetails = "";        
        if (itemCount > 0) {
            summaryDetails = itemCount + " location items";
        }        
        return summaryDetails;
    }
}
