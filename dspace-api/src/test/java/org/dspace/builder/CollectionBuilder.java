/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.builder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.MetadataSchemaEnum;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.core.Context;
import org.dspace.discovery.SearchServiceException;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.xmlworkflow.storedcomponents.CollectionRole;

/**
 * Builder to construct Collection objects
 *
 * @author Tom Desair (tom dot desair at atmire dot com)
 * @author Raf Ponsaerts (raf dot ponsaerts at atmire dot com)
 */
public class CollectionBuilder extends AbstractDSpaceObjectBuilder<Collection> {

    private Collection collection;

    protected CollectionBuilder(Context context) {
        super(context);

    }

    public static CollectionBuilder createCollection(final Context context, final Community parent) {
        CollectionBuilder builder = new CollectionBuilder(context);
        return builder.create(parent);
    }

    public static CollectionBuilder createCollection(final Context context,
                                                     final Community parent,
                                                     final String handle) {
        CollectionBuilder builder = new CollectionBuilder(context);
        return builder.create(parent, handle);
    }

    private CollectionBuilder create(final Community parent) {
        try {
            this.collection = collectionService.create(context, parent);
        } catch (Exception e) {
            return handleException(e);
        }
        return this;
    }

    private CollectionBuilder create(final Community parent, final String handle) {
        try {
            for (Collection collection : this.collectionService.findAll(context)) {
                if (collection.getHandle().equalsIgnoreCase(handle)) {
                    this.collection = collection;
                }
            }
            this.collection = this.collectionService.create(context, parent, handle);
        } catch (Exception e) {
            return handleException(e);
        }
        return this;
    }

    public CollectionBuilder withName(final String name) {
        return setMetadataSingleValue(collection, MetadataSchemaEnum.DC.getName(), "title", null, name);
    }


    public CollectionBuilder withRelationshipType(final String name) {
        return setMetadataSingleValue(collection, MetadataSchemaEnum.RELATIONSHIP.getName(), "type", null, name);
    }

    public CollectionBuilder withNameForLanguage(final String name, final String language) {
        return addMetadataValue(collection, MetadataSchemaEnum.DC.getName(), "title", null, language, name);
    }

    public CollectionBuilder withSubmissionDefinition(final String name) {
        return addMetadataValue(collection, "cris", "submission", "definition", null, name);
    }

    public CollectionBuilder withWorkflow(final String name) {
        return addMetadataValue(collection, "cris", "workflow", "name", null, name);
    }

    public CollectionBuilder withHarvestingPreTrasform(String preTransform) {
        return addMetadataValue(collection, "cris", "harvesting", "preTransform", null, preTransform);
    }

    public CollectionBuilder withHarvestingPostTrasform(String postTransform) {
        return addMetadataValue(collection, "cris", "harvesting", "postTransform", null, postTransform);
    }

    public CollectionBuilder withHarvestingEmail(String email) {
        return addMetadataValue(collection, "cris", "harvesting", "email", null, email);
    }

    public CollectionBuilder withHarvestingItemValidationEnabled() {
        return addMetadataValue(collection, "cris", "harvesting", "itemValidationEnabled", null, "true");
    }

    public CollectionBuilder withHarvestingRecordValidationEnabled() {
        return addMetadataValue(collection, "cris", "harvesting", "recordValidationEnabled", null, "true");
    }

    public CollectionBuilder withLogo(final String content) throws AuthorizeException, IOException, SQLException {

        InputStream is = IOUtils.toInputStream(content, CharEncoding.UTF_8);
        try {
            collectionService.setLogo(context, collection, is);
            return this;

        } finally {
            is.close();
        }
    }

    public CollectionBuilder withProvenance(final String provenance) {
        return addMetadataValue(collection,
                                MetadataSchemaEnum.DC.getName(),
                                "description",
                                "provenance",
                                provenance);
    }

    public CollectionBuilder withTemplateItem() throws SQLException, AuthorizeException {
        collectionService.createTemplateItem(context, collection);
        return this;
    }

    /**
     * Create a submitter group for the collection with the specified members
     *
     * @param members epersons to add to the submitter group
     * @return this builder
     * @throws SQLException
     * @throws AuthorizeException
     */
    public CollectionBuilder withSubmitterGroup(EPerson... members) throws SQLException, AuthorizeException {
        Group g = collectionService.createSubmitters(context, collection);
        for (EPerson e : members) {
            groupService.addMember(context, g, e);
        }
        groupService.update(context, g);
        return this;
    }

    /**
     * Create a submitter group for the collection with the specified group members
     *
     * @param members groups to add to the submitter group
     * @return this builder
     * @throws SQLException
     * @throws AuthorizeException
     */
    public CollectionBuilder withSubmitterGroup(Group... members) throws SQLException, AuthorizeException {
        Group g = collectionService.createSubmitters(context, collection);
        for (Group m : members) {
            groupService.addMember(context, g, m);
        }
        groupService.update(context, g);
        return this;
    }

    public CollectionBuilder withWorkflowGroup(int step, EPerson... members) throws SQLException, AuthorizeException {
        Group g = collectionService.createWorkflowGroup(context, collection, step);
        for (EPerson e : members) {
            groupService.addMember(context, g, e);
        }
        groupService.update(context, g);
        return this;
    }

    public CollectionBuilder withWorkflowGroup(int step, Group... members) throws SQLException, AuthorizeException {
        Group g = collectionService.createWorkflowGroup(context, collection, step);
        for (Group m : members) {
            groupService.addMember(context, g, m);
        }
        groupService.update(context, g);
        return this;
    }

    /**
     * Create an admin group for the collection with the specified members
     *
     * @param members epersons to add to the admin group
     * @return this builder
     * @throws SQLException
     * @throws AuthorizeException
     */
    public CollectionBuilder withAdminGroup(EPerson... members) throws SQLException, AuthorizeException {
        Group g = collectionService.createAdministrators(context, collection);
        for (EPerson e : members) {
            groupService.addMember(context, g, e);
        }
        groupService.update(context, g);
        return this;
    }

    /**
     * Create an admin group for the collection with the specified group members
     *
     * @param members groups to add to the admin group
     * @return this builder
     * @throws SQLException
     * @throws AuthorizeException
     */
    public CollectionBuilder withAdminGroup(Group... members) throws SQLException, AuthorizeException {
        Group g = collectionService.createAdministrators(context, collection);
        for (Group m : members) {
            groupService.addMember(context, g, m);
        }
        groupService.update(context, g);
        return this;
    }


    public CollectionBuilder withRoleGroup(String roleId, Group... members) throws SQLException, AuthorizeException {

        Group group = GroupBuilder.createGroup(context)
            .withName("COLLECTION_" + collection.getID() + "_" + roleId)
            .build();

        CollectionRole colRole = collectionRoleService.find(context, collection, roleId);
        if (colRole == null) {
            colRole = collectionRoleService.create(context, collection, roleId, group);
        }

        for (Group member : members) {
            groupService.addMember(context, group, member);
        }
        groupService.update(context, group);

        colRole.setGroup(group);
        collectionRoleService.update(context, colRole);
        return this;
    }

    public CollectionBuilder withSharedWorkspace() {
        return setMetadataSingleValue(collection, "cris", "workspace", "shared", "true");
    }

    @Override
    public Collection build() {
        try {
            collectionService.update(context, collection);
            context.dispatchEvents();
            indexingService.commit();

        } catch (Exception e) {
            return handleException(e);
        }
        return collection;
    }

    @Override
    public void cleanup() throws Exception {
       try (Context c = new Context()) {
            c.turnOffAuthorisationSystem();
            // Ensure object and any related objects are reloaded before checking to see what needs cleanup
            collection = c.reloadEntity(collection);
            if (collection != null) {
                deleteAdminGroup(c);
                deleteItemTemplate(c);
                deleteDefaultReadGroups(c, collection);
                deleteWorkflowGroups(c, collection);
                delete(c ,collection);
                c.complete();
            }
       }
    }

    private void deleteAdminGroup(Context c) throws SQLException, AuthorizeException, IOException {
        Group group = collection.getAdministrators();
        if (group != null) {
            collectionService.removeAdministrators(c, collection);
            groupService.delete(c, group);
        }
    }

    private void deleteItemTemplate(Context c) throws SQLException, AuthorizeException, IOException {
        if (collection.getTemplateItem() != null) {
                 collectionService.removeTemplateItem(c, collection);
        }
    }

    public void deleteWorkflowGroups(Context c, Collection collection) throws Exception {
        List<CollectionRole> collectionRoles = collectionRoleService.findByCollection(c, collection);
        for (CollectionRole collectionRole : collectionRoles) {
            Group group = collectionRole.getGroup();
            collectionRoleService.delete(c, collectionRole);
            groupService.delete(c, group);
        }
    }

    public void deleteDefaultReadGroups(Context c, Collection collection) throws Exception {
        Group defaultItemReadGroup = groupService.findByName(c, "COLLECTION_" +
              collection.getID().toString() + "_ITEM_DEFAULT_READ");
        Group defaultBitstreamReadGroup = groupService.findByName(c, "COLLECTION_" +
              collection.getID().toString() + "_BITSTREAM_DEFAULT_READ");
        if (defaultItemReadGroup != null) {
            groupService.delete(c, defaultItemReadGroup);
        }
        if (defaultBitstreamReadGroup != null) {
            groupService.delete(c, defaultBitstreamReadGroup);
        }
    }

    /**
     * Delete the Test Collection referred to by the given UUID
     *
     * @param uuid UUID of Test Collection to delete
     * @throws SQLException
     * @throws IOException
     * @throws SearchServiceException
     */
    public static void deleteCollection(UUID uuid) throws SQLException, IOException, SearchServiceException {
       try (Context c = new Context()) {
            c.turnOffAuthorisationSystem();
            Collection collection = collectionService.find(c, uuid);
            if (collection != null) {
                try {
                    collectionService.delete(c, collection);
                } catch (AuthorizeException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            c.complete();
            indexingService.commit();
       }
    }

    @Override
    protected DSpaceObjectService<Collection> getService() {
        return collectionService;
    }
}
