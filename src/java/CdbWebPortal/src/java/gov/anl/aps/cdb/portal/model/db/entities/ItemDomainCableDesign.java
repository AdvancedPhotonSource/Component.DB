/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.model.db.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gov.anl.aps.cdb.common.exceptions.CdbException;
import gov.anl.aps.cdb.portal.constants.ItemDomainName;
import gov.anl.aps.cdb.portal.constants.ItemElementRelationshipTypeNames;
import gov.anl.aps.cdb.portal.controllers.ItemDomainCableCatalogController;
import gov.anl.aps.cdb.portal.controllers.ItemDomainMachineDesignController;
import gov.anl.aps.cdb.portal.controllers.RelationshipTypeController;
import gov.anl.aps.cdb.portal.model.db.beans.RelationshipTypeFacade;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author cmcchesney
 */
@Entity
@DiscriminatorValue(value = ItemDomainName.CABLE_DESIGN_ID + "")
public class ItemDomainCableDesign extends Item {

    private static final Logger LOGGER = LogManager.getLogger(ItemDomainCableDesign.class.getName());

    private transient String externalCableName = null;
    private transient String importCableId = null;
    private transient String alternateCableId = null;
    private transient String legacyQrId = null;
    private transient String laying = null;
    private transient String voltage = null;
    private transient String endpoint1Description = null;
    private transient String endpoint2Description = null;

    public final static String CABLE_DESIGN_INTERNAL_PROPERTY_TYPE = "cable_design_internal_property_type";
    public final static String CABLE_DESIGN_PROPERTY_EXT_CABLE_NAME_KEY = "externalCableName";
    public final static String CABLE_DESIGN_PROPERTY_IMPORT_CABLE_ID_KEY = "importCableId";
    public final static String CABLE_DESIGN_PROPERTY_ALT_CABLE_ID_KEY = "alternateCableId";
    public final static String CABLE_DESIGN_PROPERTY_LEGACY_QR_ID_KEY = "legacyQrId";
    public final static String CABLE_DESIGN_PROPERTY_LAYING_KEY = "laying";
    public final static String CABLE_DESIGN_PROPERTY_VOLTAGE_KEY = "voltage";
    public final static String CABLE_DESIGN_PROPERTY_ENDPOINT1_DESC_KEY = "endpoint1Description";
    public final static String CABLE_DESIGN_PROPERTY_ENDPOINT2_DESC_KEY = "endpoint2Description";

    private static final String endpointsSeparator = " | ";

    @Override
    public Item createInstance() {
        return new ItemDomainCableDesign();
    }

    public List<Item> getEndpointList() {
        {
            ItemElement selfElement = this.getSelfElement();
            List<ItemElementRelationship> ierList
                    = selfElement.getItemElementRelationshipList1();
            if (ierList != null) {
                // find just the cable relationship items
                RelationshipType cableIerType
                        = RelationshipTypeFacade.getInstance().findByName(
                                ItemElementRelationshipTypeNames.itemCableConnection.getValue());
                if (cableIerType != null) {
                    return ierList.stream().
                            filter(ier -> ier.getRelationshipType().getName().equals(cableIerType.getName())).
                            sorted((ier1,ier2) -> (ier1.getSecondSortOrder() == null || ier2.getSecondSortOrder() == null) ? 0 : ier1.getSecondSortOrder().compareTo(ier2.getSecondSortOrder())).
                            map(ier -> ier.getFirstItemElement().getParentItem()).
                            collect(Collectors.toList());
                }
            }

            return null;
        }
    }

    private RelationshipType getCableConnectionRelationshipType() {
        RelationshipType relationshipType
                = RelationshipTypeFacade.getInstance().findByName(
                        ItemElementRelationshipTypeNames.itemCableConnection.getValue());
        if (relationshipType == null) {
            RelationshipTypeController controller = RelationshipTypeController.getInstance();
            String name = ItemElementRelationshipTypeNames.itemCableConnection.getValue();
            relationshipType = controller.createRelationshipTypeWithName(name);
        }
        return relationshipType;
    }

    /**
     * Creates ItemElementRelationship for the 2 specified items.
     *
     * @param item Machine design item for cable endpoint.
     * @return New instance of ItemElementRelationshipo for specified items.
     */
    private ItemElementRelationship createRelationship(Item item, float sortOrder) {

        ItemElementRelationship itemElementRelationship = new ItemElementRelationship();
        itemElementRelationship.setFirstItemElement(item.getSelfElement());
        itemElementRelationship.setSecondItemElement(this.getSelfElement());
        itemElementRelationship.setSecondSortOrder(sortOrder);

        RelationshipType cableConnectionRelationshipType = getCableConnectionRelationshipType();
        itemElementRelationship.setRelationshipType(cableConnectionRelationshipType);

        return itemElementRelationship;
    }

    /**
     * Adds specified relationship for specified item.
     *
     * @param item Item to add relationship for.
     * @param ier Relationship to add.
     * @param secondItem True if the item is the second item in the
     * relationship.
     */
    private void addItemElementRelationshipToItem(Item item, ItemElementRelationship ier, boolean secondItem) {
        ItemElement selfElement = item.getSelfElement();
        List<ItemElementRelationship> ierList;
        if (secondItem) {
            ierList = selfElement.getItemElementRelationshipList1();
        } else {
            ierList = selfElement.getItemElementRelationshipList();
        }
        ierList.add(ier);
    }

    private void addCableRelationship(Item endpoint, float sortOrder) {
        // create relationships from cable to endpoints
        ItemElementRelationship relationship = createRelationship(endpoint, sortOrder);

        // Create list for cable's relationships. 
        ItemElement selfElement = this.getSelfElement();
        if (selfElement.getItemElementRelationshipList1() == null) {
            selfElement.setItemElementRelationshipList1(new ArrayList<>());
        }

        // Add appropriate item relationships to model.
        addItemElementRelationshipToItem(endpoint, relationship, false);
        addItemElementRelationshipToItem(this, relationship, true);
    }

    public void setEndpoint1(Item itemEndpoint1) {
        this.addCableRelationship(itemEndpoint1, 1.0f);
    }

    public void setEndpoint1Id(String id) {
        ItemDomainMachineDesign itemEndpoint1 = (ItemDomainMachineDesign)(getEntityById(ItemDomainMachineDesignController.getInstance(), id));
        if (itemEndpoint1 != null) {
            setEndpoint1(itemEndpoint1);
        } else {
            LOGGER.error("setEndpoint1Id() unknown machine design item id " + id);
        }
    }

    public void setEndpoint2(Item itemEndpoint2) {
        this.addCableRelationship(itemEndpoint2, 2.0f);
    }

    public void setEndpoint2Id(String id) {
        ItemDomainMachineDesign itemEndpoint2 = (ItemDomainMachineDesign)(getEntityById(ItemDomainMachineDesignController.getInstance(), id));
        if (itemEndpoint2 != null) {
            setEndpoint2(itemEndpoint2);
        } else {
            LOGGER.error("setEndpoint2Id() unknown machine design item id " + id);
        }
    }

    /**
     * Updates oldEndpoint to newEndpoint.
     *
     * @param oldEndpoint
     * @param newEndpoint
     */
    public Boolean updateEndpoint(Item oldEndpoint, Item newEndpoint) {

        ItemElement selfElement = this.getSelfElement();
        List<ItemElementRelationship> ierList = selfElement.getItemElementRelationshipList1();

        if (ierList != null) {

            RelationshipType cableIerType
                    = RelationshipTypeFacade.getInstance().findByName(
                            ItemElementRelationshipTypeNames.itemCableConnection.getValue());

            // find cable relationship for old endpoint
            ItemElementRelationship cableRelationship = ierList.stream()
                    .filter(ier -> (ier.getRelationshipType().getName().equals(cableIerType.getName()))
                    && (ier.getFirstItemElement().equals(oldEndpoint.getSelfElement())))
                    .findAny()
                    .orElse(null);

            // update cable relationship to new endpoint
            if (cableRelationship != null) {
                cableRelationship.setFirstItemElement(newEndpoint.getSelfElement());
                // null out connector too, for when we add support for port-level connections
                cableRelationship.setFirstItemConnector(null);
            }
        }

        return true;
    }

    /**
     * Returns a string containing the cables endpoints for display.
     */
    public String getEndpointsString() {
        String result = "";
        int count = 0;
        List<Item> iList = this.getEndpointList();
        for (Item endpoint : iList) {
            count = count + 1;
            result = result + endpoint.getName();
            if (count != iList.size()) {
                result = result + endpointsSeparator;
            }
        }
        return result;
    }

    public Item getEndpoint1() {
        List<Item> iList = this.getEndpointList();
        if ((iList != null) && (iList.size() > 0)) {
            return iList.get(0);
        } else {
            return null;
        }
    }

    public String getEndpoint1String() {
        Item iEndpoint1 = this.getEndpoint1();
        if (iEndpoint1 != null) {
            return iEndpoint1.getName();
        } else {
            return "";
        }
    }

    public Item getEndpoint2() {
        List<Item> iList = this.getEndpointList();
        if ((iList != null) && (iList.size() > 0)) {
            return iList.get(1);
        } else {
            return null;
        }
    }

    public String getEndpoint2String() {
        Item iEndpoint2 = this.getEndpoint2();
        if (iEndpoint2 != null) {
            return iEndpoint2.getName();
        } else {
            return "";
        }
    }

    public void setCatalogItem(Item itemCableCatalog) {
        // "assign" catalog item to cable design
        ItemElement selfElement = this.getSelfElement();
        selfElement.setContainedItem2(itemCableCatalog);
    }

    public void setCatalogItemId(String catalogItemId) {
        ItemDomainCableCatalog catalogItem = (ItemDomainCableCatalog) (getEntityById(ItemDomainCableCatalogController.getInstance(), catalogItemId));

        if (catalogItem != null) {
            setCatalogItem(catalogItem);
        } else {
            LOGGER.error("setCatalogItemId() unknown cable catalog item id " + catalogItemId);
        }
     }

    public Item getCatalogItem() {
        ItemElement selfElementCable = this.getSelfElement();
        return selfElementCable.getContainedItem2();
    }

    public String getCatalogItemString() {
        Item iCatalog = this.getCatalogItem();
        if (iCatalog != null) {
            return iCatalog.getName();
        } else {
            return "";
        }
    }

    private PropertyValue getInternalCableDesignPropertyValue() {
        List<PropertyValue> propertyValueList = getPropertyValueList();
        for (PropertyValue propertyValue : propertyValueList) {
            if (propertyValue.getPropertyType().getName().equals(CABLE_DESIGN_INTERNAL_PROPERTY_TYPE)) {
                return propertyValue;
            }
        }
        return null;
    }

    public String getAlternateName() {
        return getItemIdentifier1();
    }

    public void setAlternateName(String n) {
        setItemIdentifier1(n);
    }

    @JsonIgnore
    public String getExternalCableName() throws CdbException {
        if (externalCableName == null) {
            externalCableName = getCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_EXT_CABLE_NAME_KEY);
        }
        return externalCableName;
    }

    public void setExternalCableName(String n) throws CdbException {
        externalCableName = n;
        setCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_EXT_CABLE_NAME_KEY, n);
    }

    @JsonIgnore
    public String getImportCableId() throws CdbException {
        if (importCableId == null) {
            importCableId = getCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_IMPORT_CABLE_ID_KEY);
        }
        return importCableId;
    }

    public void setImportCableId(String id) throws CdbException {
        importCableId = id;
        setCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_IMPORT_CABLE_ID_KEY, id);
    }

    @JsonIgnore
    public String getAlternateCableId() throws CdbException {
        if (alternateCableId == null) {
            alternateCableId = getCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_ALT_CABLE_ID_KEY);
        }
        return alternateCableId;
    }

    public void setAlternateCableId(String id) throws CdbException {
        alternateCableId = id;
        setCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_ALT_CABLE_ID_KEY, id);
    }

    @JsonIgnore
    public String getLegacyQrId() throws CdbException {
        if (legacyQrId == null) {
            legacyQrId = getCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_LEGACY_QR_ID_KEY);
        }
        return legacyQrId;
    }

    public void setLegacyQrId(String id) throws CdbException {
        legacyQrId = id;
        setCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_LEGACY_QR_ID_KEY, id);
    }

    @JsonIgnore
    public String getLaying() throws CdbException {
        if (laying == null) {
            laying = getCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_LAYING_KEY);
        }
        return laying;
    }

    public void setLaying(String l) throws CdbException {
        laying = l;
        setCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_LAYING_KEY, l);
    }

    @JsonIgnore
    public String getVoltage() throws CdbException {
        if (voltage == null) {
            voltage = getCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_VOLTAGE_KEY);
        }
        return voltage;
    }

    public void setVoltage(String v) throws CdbException {
        voltage = v;
        setCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_VOLTAGE_KEY, v);
    }

    @JsonIgnore
    public String getEndpoint1Description() throws CdbException {
        if (endpoint1Description == null) {
            endpoint1Description = getCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_ENDPOINT1_DESC_KEY);
        }
        return endpoint1Description;
    }

    public void setEndpoint1Description(String d) throws CdbException {
        endpoint1Description = d;
        setCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_ENDPOINT1_DESC_KEY, d);
    }

    @JsonIgnore
    public String getEndpoint2Description() throws CdbException {
        if (endpoint2Description == null) {
            endpoint2Description = getCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_ENDPOINT2_DESC_KEY);
        }
        return endpoint2Description;
    }

    public void setEndpoint2Description(String d) throws CdbException {
        endpoint2Description = d;
        setCoreMetadataPropertyFieldValue(CABLE_DESIGN_PROPERTY_ENDPOINT2_DESC_KEY, d);
    }

    public void setTechnicalSystemList(List<ItemCategory> technicalSystemList) {
        setItemCategoryList(technicalSystemList);
    }

}
