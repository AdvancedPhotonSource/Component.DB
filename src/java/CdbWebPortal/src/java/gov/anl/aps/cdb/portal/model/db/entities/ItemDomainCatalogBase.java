/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.portal.model.db.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author djarosz
 */
public abstract class ItemDomainCatalogBase<InventoryItem extends Item> extends Item {
    
    private transient String sourceString = null;

    @JsonIgnore
    public List<InventoryItem> getInventoryItemList() {
        return (List<InventoryItem>)(List<?>) super.getDerivedFromItemList(); 
    }
    
    @JsonIgnore
    public String getSourceString() {
        return sourceString;
    }
    
    public void setManufacturerInfo(Source source, String partNum) {
        if (source != null) {
            List<ItemSource> itemSourceList = new ArrayList<>();
            ItemSource itemSource = new ItemSource();
            itemSource.setItem(this);
            itemSource.setSource(source);
            if ((partNum != null) && (!partNum.isBlank())) {
                itemSource.setPartNumber(partNum);
            }
            itemSource.setIsManufacturer(true);
            itemSourceList.add(itemSource);
            this.setItemSourceList(itemSourceList);
            sourceString = source.getName();
        }
    }
    
    @JsonIgnore
    public String getPartNumber() {
        return this.getItemIdentifier1();
    }
    
    public void setPartNumber(String n) {
        this.setItemIdentifier1(n);
    }
    
}
