/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.import_export.import_.helpers;

import gov.anl.aps.cdb.common.exceptions.CdbException;
import gov.anl.aps.cdb.portal.constants.EntityTypeName;
import gov.anl.aps.cdb.portal.controllers.ItemDomainMachineDesignController;
import gov.anl.aps.cdb.portal.import_export.import_.objects.CreateInfo;
import gov.anl.aps.cdb.portal.import_export.import_.objects.specs.ColumnSpec;
import gov.anl.aps.cdb.portal.import_export.import_.objects.specs.IdOrNameRefColumnSpec;
import gov.anl.aps.cdb.portal.import_export.import_.objects.specs.StringColumnSpec;
import gov.anl.aps.cdb.portal.model.db.beans.ItemDomainMachineDesignFacade;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainMachineDesign;
import gov.anl.aps.cdb.portal.model.db.entities.ItemElement;
import gov.anl.aps.cdb.portal.model.db.entities.UserGroup;
import gov.anl.aps.cdb.portal.model.db.entities.UserInfo;
import gov.anl.aps.cdb.portal.view.objects.KeyValueObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author craig
 */
public class ImportHelperMachineTemplateInstantiation extends ImportHelperTreeViewBase<ItemDomainMachineDesign, ItemDomainMachineDesignController> {
    
    private static final Logger LOGGER = LogManager.getLogger(ImportHelperMachineTemplateInstantiation.class.getName());
    
    private static final String KEY_CONTAINER = "importMdItem";
    private static final String KEY_TEMPLATE_INVOCATION = "importTemplateAndParameters";

    private static final String HEADER_PARENT = "Parent ID";
    private static final String HEADER_TEMPLATE_INVOCATION = "Template Instantiation";
    private static final String HEADER_ALT_NAME = "Machine Design Alternate Name";
    private static final String HEADER_DESCRIPTION = "Machine Design Item Description";
    
    private Map<String, ItemDomainMachineDesign> itemByNameMap = new HashMap<>();

    private int templateInstantiationCount = 0;
    
    protected List<ColumnSpec> getColumnSpecs() {
        
        List<ColumnSpec> specs = new ArrayList<>();
        
        specs.add(new IdOrNameRefColumnSpec(HEADER_PARENT, KEY_CONTAINER, "setImportMdItem", true, "CDB ID or name of parent machine design item.  Can only be provided for level 0 item. Name must be unique and prefixed with '#'.", ItemDomainMachineDesignController.getInstance(), ItemDomainMachineDesign.class, ""));
        specs.add(new StringColumnSpec(HEADER_TEMPLATE_INVOCATION, KEY_TEMPLATE_INVOCATION, "setImportTemplateAndParameters", true, "Template to instantiate with required parameters, e.g., 'PS-SR-S{nn}-CAB1(nn=24)'.", 0));        
        specs.add(new StringColumnSpec(HEADER_ALT_NAME, "alternateName", "setAlternateName", false, "Alternate name.", 128));
        specs.add(new StringColumnSpec(HEADER_DESCRIPTION, "description", "setDescription", false, "Textual description.", 256));
        specs.add(projectListColumnSpec());
        specs.add(ownerUserColumnSpec());
        specs.add(ownerGroupColumnSpec());

        return specs;
    }
    
    @Override
    public ItemDomainMachineDesignController getEntityController() {
        return ItemDomainMachineDesignController.getInstance();
    }

    @Override
    public String getTemplateFilename() {
        return "Machine Template Instantiation Template";
    }
    
    @Override
    protected void reset_() {
        itemByNameMap = new HashMap<>();
        templateInstantiationCount = 0;
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

        String methodLogName = "createEntityInstance() ";
        boolean isValid = true;
        String validString = "";
        ItemDomainMachineDesign invalidInstance = getEntityController().createEntityInstance();
        
        String templateParams = (String)rowMap.get(KEY_TEMPLATE_INVOCATION);
        if ((templateParams == null) || (!templateParams.isEmpty())) {
            
        }

        // check that cell value matches pattern, basically of the form "*(*)"
        String templateRegex = "[^\\(]*\\([^\\)]*\\)";
        if (!templateParams.matches(templateRegex)) {
            // invalid cell value doesn't match pattern
            isValid = false;
            validString = "invalid format for template and parameters: " + templateParams;
            LOGGER.info(methodLogName + validString);
            return new CreateInfo(invalidInstance, isValid, validString);
        }

        int indexOpenParen = templateParams.indexOf('(');
        int indexCloseParen = templateParams.indexOf(')');

        // parse and validate template name
        String templateName = templateParams.substring(0, indexOpenParen);
        if ((templateName == null) || (templateName.isEmpty())) {
            // unspecified template name
            isValid = false;
            validString = "template name not provided: " + templateParams;
            LOGGER.info(methodLogName + validString);
            return new CreateInfo(invalidInstance, isValid, validString);
        }

        // parse and validate variable string
        String templateVarString = templateParams.substring(indexOpenParen + 1, indexCloseParen);
        Map<String, String> varNameValueMap = new HashMap<>();
        // iterate through comma separated list of "varName=value" pairs
        String[] varArray = templateVarString.split(",");
        for (String varNameValue : varArray) {
            String[] nameValueArray = varNameValue.split("=");
            if (nameValueArray.length != 2) {
                // invalid format
                isValid = false;
                validString = "invalid format for template parameters: " + templateVarString;
                LOGGER.info(methodLogName + validString);
                return new CreateInfo(invalidInstance, isValid, validString);
            }

            String varName = nameValueArray[0].strip();
            String varValue = nameValueArray[1].strip();
            varNameValueMap.put(varName, varValue);
        }

        invalidInstance.setName(templateName);
        
        // check for and set parent item
        ItemDomainMachineDesign itemParent = (ItemDomainMachineDesign) rowMap.get(KEY_CONTAINER);
        if (itemParent == null) {
            // must specify parent for template invocation
            isValid = false;
            validString = "parent ID must be specified for template invocation";
            LOGGER.info(methodLogName + validString);
            return new CreateInfo(invalidInstance, isValid, validString);
        }
        invalidInstance.setImportMdItem(itemParent);

        // retrieve specified template
        ItemDomainMachineDesign templateItem;
        try {
            templateItem
                    = ItemDomainMachineDesignFacade.getInstance().findUniqueByDomainAndEntityTypeAndName(
                            templateName, 
                            EntityTypeName.template.getValue(),
                            null);
        } catch (CdbException ex) {
            isValid = false;
            validString
                    = "exception retrieving specified template, possibly non-unique name: "
                    + templateName + ": " + ex;
            LOGGER.info(methodLogName + validString);
            return new CreateInfo(invalidInstance, isValid, validString);
        }

        if (templateItem == null) {
            isValid = false;
            validString = "no template found for name: " + templateName;
            LOGGER.info(methodLogName + validString);
            return new CreateInfo(invalidInstance, isValid, validString);
        }

        // check that it's a template
        if (!templateItem.getIsItemTemplate()) {
            isValid = false;
            validString = "specified template name is not a template: " + templateName;
            LOGGER.info(methodLogName + validString);
            return new CreateInfo(invalidInstance, isValid, validString);
        }

        // generate list of variable name/value pairs
        getEntityController().setMachineDesignNameList(new ArrayList<>());
        getEntityController().generateMachineDesignTemplateNameVarsRecursivelly(templateItem);
        List<KeyValueObject> varNameList = getEntityController().getMachineDesignNameList();
        for (KeyValueObject obj : varNameList) {
            // check that all params in template are specified in import params
            if (!varNameValueMap.containsKey(obj.getKey())) {
                // import params do not include a param specified for template
                isValid = false;
                validString = "specified template parameters missing required variable: " + obj.getKey();
                LOGGER.info(methodLogName + validString);
                return new CreateInfo(invalidInstance, isValid, validString);
            }

            obj.setValue(varNameValueMap.get(obj.getKey()));
        }
        
        UserInfo user = (UserInfo) rowMap.get(KEY_USER);
        UserGroup group = (UserGroup) rowMap.get(KEY_GROUP);

        ItemDomainMachineDesign item = 
                ItemDomainMachineDesign.importInstantiateTemplateUnderParent(
                        templateItem, itemParent, user, group);
        
        // update tree view with item and parent
        updateTreeView(item, itemParent, true);

        // add entry to name map for new item
        itemByNameMap.put(item.getName(), item);
        
        templateInstantiationCount = templateInstantiationCount + 1;

        return new CreateInfo(item, isValid, validString);
    }
    
    @Override
    protected String getCustomSummaryDetails() {        
        String summaryDetails = "";        
        if (templateInstantiationCount > 0) {
            summaryDetails = 
                    templateInstantiationCount + " template instantiations including " +
                    getTreeNodeChildCount() + "  items";                    
        }
        return summaryDetails;
    }
    
}
