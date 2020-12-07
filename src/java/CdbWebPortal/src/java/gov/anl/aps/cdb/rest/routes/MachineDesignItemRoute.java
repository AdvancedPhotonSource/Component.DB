/*
 * Copyright (c) UChicago Argonne, LLC. All rights reserved.
 * See LICENSE file.
 */
package gov.anl.aps.cdb.rest.routes;

import gov.anl.aps.cdb.common.exceptions.CdbException;
import gov.anl.aps.cdb.common.exceptions.InvalidArgument;
import gov.anl.aps.cdb.common.exceptions.ObjectNotFound;
import gov.anl.aps.cdb.portal.controllers.ItemController;
import gov.anl.aps.cdb.portal.controllers.ItemDomainMachineDesignController;
import gov.anl.aps.cdb.portal.model.db.beans.ItemDomainMachineDesignFacade;
import gov.anl.aps.cdb.portal.model.db.entities.Item;
import gov.anl.aps.cdb.portal.model.db.entities.ItemDomainMachineDesign;
import gov.anl.aps.cdb.portal.model.db.entities.UserInfo;
import gov.anl.aps.cdb.rest.authentication.Secured;
import gov.anl.aps.cdb.rest.entities.ItemDomainMdSearchResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.TreeNode;

/**
 *
 * @author craig
 */
@Path("/MachineDesignItems")
@Tag(name = "machineDesignItems")
public class MachineDesignItemRoute extends ItemBaseRoute {

    private static final Logger LOGGER = LogManager.getLogger(MachineDesignItemRoute.class.getName());

    @EJB
    ItemDomainMachineDesignFacade facade;

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ItemDomainMachineDesign> getMachineDesignItemList() {
        LOGGER.debug("Fetching machine design list");
        return facade.findAll();
    }

    @GET
    @Path("/ById/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ItemDomainMachineDesign getMachineDesignItemById(@PathParam("id") int id) throws ObjectNotFound {
        LOGGER.debug("Fetching item with id: " + id);
        ItemDomainMachineDesign item = facade.find(id);
        if (item == null) {
            ObjectNotFound ex = new ObjectNotFound("Could not find item with id: " + id);
            LOGGER.error(ex);
            throw ex;
        }
        return item;
    }

    @GET
    @Path("/ByName/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ItemDomainMachineDesign> getMachineDesignItemsByName(@PathParam("name") String name) throws ObjectNotFound {
        LOGGER.debug("Fetching items with name: " + name);
        List<ItemDomainMachineDesign> itemList = facade.findByName(name);
        if (itemList == null || itemList.isEmpty()) {
            ObjectNotFound ex = new ObjectNotFound("Could not find item with name: " + name);
            LOGGER.error(ex);
            throw ex;
        }
        return itemList;
    }

    /**
     * Searches the top-level machine design hierarchy "root" node for children
     * with specified name.
     *
     * @throws ObjectNotFound
     */
    @GET
    @Path("/ByRootAndName/{root}/{container}/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ItemDomainMachineDesign> getMdInHierarchyByName(
            @PathParam("root") String rootItemName, 
            @PathParam("container") String containerItemName, 
            @PathParam("name") String itemName) throws InvalidArgument {
        
        LOGGER.debug("Fetching item in hiearchy: " + rootItemName + " in container: " + containerItemName + " named: " + itemName);
        
        if ((rootItemName == null) || (rootItemName.isBlank())) {
            throw new InvalidArgument(("must specify root item name"));
        }
        
        if ((containerItemName == null) || (containerItemName.isBlank())) {
            throw new InvalidArgument(("must specify container item name"));
        }
        
        List<ItemDomainMachineDesign> itemList = facade.findByName(itemName);

        // eliminate items whose top-level parent is not the specified "root" node
        List<ItemDomainMachineDesign> result = new ArrayList<>();
        for (ItemDomainMachineDesign item : itemList) {

            // walk up hierarchy to top-level "root" parent
            ItemDomainMachineDesign parentItem = item.getParentMachineDesign();
            boolean foundContainer = false;
            boolean foundRoot = false;
            while (parentItem != null) {
                
                // check container match
                String alternateName = parentItem.getAlternateName();
                if (alternateName == null) {
                    alternateName = "";
                }
                if ((parentItem.getName().equals(containerItemName)) ||
                        (alternateName.equals(containerItemName))) { 
                    foundContainer = true;
                }
                
                // check root match
                if ((parentItem.getParentMachineDesign() == null) && 
                        (parentItem.getName().equals(rootItemName))) {
                    foundRoot = true;
                }
                
                parentItem = parentItem.getParentMachineDesign();
            }

            if (foundContainer && foundRoot) {
                result.add(item);
            }
        }

        return result;
    }

    @GET
    @Path("/DetailedMachineDesignSearch/{searchText}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ItemDomainMdSearchResult> getDetailedMdSearchResults(@PathParam("searchText") String searchText) throws ObjectNotFound, InvalidArgument {
        LOGGER.debug("Performing a detailed machine design item search for search query: " + searchText);

        ItemDomainMachineDesignController mdInstance = ItemDomainMachineDesignController.getApiInstance();

        TreeNode rootNode = mdInstance.getSearchResults(searchText, true);

        List<TreeNode> children = rootNode.getChildren();
        List<ItemDomainMdSearchResult> itemHierarchy = new ArrayList<>();
        for (TreeNode child : children) {
            ItemDomainMdSearchResult hierarchy = new ItemDomainMdSearchResult(child);
            itemHierarchy.add(hierarchy);
        }

        return itemHierarchy;
    }

    @POST
    @Path("/UpdateAssignedItem/{mdItemId}/{assignedItemId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update assigned item in a machine design item.")
    @SecurityRequirement(name = "cdbAuth")
    @Secured
    public ItemDomainMachineDesign updateAssignedItem(@PathParam("mdItemId") int mdItemId, @PathParam("assignedItemId") Integer assignedItemId) throws ObjectNotFound, CdbException {
        Item currentItem = getItemByIdBase(mdItemId);
        Item assignedItem = null;
        if (assignedItemId != null) { 
            assignedItem = getItemByIdBase(assignedItemId);
        }

        if (currentItem instanceof ItemDomainMachineDesign == false) {
            throw new InvalidArgument("Item with id " + mdItemId + " is not of type Machine Design");
        }

        UserInfo currentUser = verifyCurrentUserPermissionForItem(currentItem);
        ItemDomainMachineDesign mdItem = (ItemDomainMachineDesign) currentItem;
        
        mdItem.setAssignedItem(assignedItem);
        
        ItemController itemDomainController = mdItem.getItemDomainController();
        
        itemDomainController.updateFromApi(mdItem, currentUser);
        
        return getMachineDesignItemById(mdItemId);
    }
    
    @POST
    @Path("/ClearAssignedItem/{mdItemId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update assigned item in a machine design item.")
    @SecurityRequirement(name = "cdbAuth")
    @Secured
    public ItemDomainMachineDesign clearAssignedItem(@PathParam("mdItemId") int mdItemId) throws CdbException {
        return updateAssignedItem(mdItemId, null); 
    }
}
