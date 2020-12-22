/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.app.profile;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.dspace.app.profile.service.AfterImportAction;
import org.dspace.app.profile.service.ImportResearcherProfileService;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.service.InstallItemService;
import org.dspace.core.Context;
import org.dspace.external.model.ExternalDataObject;
import org.dspace.external.service.ExternalDataService;
import org.dspace.services.RequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Corrado Lombardi (corrado.lombardi at 4science.it)
 */
public class ImportResearcherProfileServiceImpl implements ImportResearcherProfileService {

    private static final Logger log = LoggerFactory.getLogger(ImportResearcherProfileServiceImpl.class);

    private final ExternalDataService externalDataService;

    private final InstallItemService installItemService;

    private final RequestService requestService;

    private List<AfterImportAction> afterImportActionList;


    public ImportResearcherProfileServiceImpl(ExternalDataService externalDataService,
                                              InstallItemService installItemService,
                                              RequestService requestService) {
        this.externalDataService = externalDataService;
        this.installItemService = installItemService;
        this.requestService = requestService;
    }

    @Override
    public Item importProfile(Context context, URI source, Collection collection)
        throws AuthorizeException, SQLException {

        ResearcherProfileSource researcherProfileSource = new ResearcherProfileSource(source);
        requestService.getCurrentRequest().setAttribute("context", context);
        Optional<ExternalDataObject> externalDataObject = externalDataService
            .getExternalDataObject(researcherProfileSource.source(), researcherProfileSource.id());

        if (externalDataObject.isEmpty()) {
            throw new ResourceNotFoundException("resource for uri " + source + " not found");
        }
        return createItem(context, collection, externalDataObject.get());
    }

    public void setAfterImportActionList(List<AfterImportAction> afterImportActionList) {
        this.afterImportActionList = afterImportActionList;
    }

    private Item createItem(Context context, Collection collection, ExternalDataObject externalDataObject)
        throws AuthorizeException, SQLException {
        try {
            WorkspaceItem workspaceItem = externalDataService.createWorkspaceItemFromExternalDataObject(context,
                externalDataObject,
                collection);
            Item item = installItemService.installItem(context, workspaceItem);
            applyAfterImportActions(context, item, externalDataObject);
            return item;
        } catch (AuthorizeException | SQLException e) {
            log.error("Error while importing item into collection {}", e.getMessage(), e);
            throw e;
        }
    }

    private void applyAfterImportActions(Context context, Item item, ExternalDataObject externalDataObject)
        throws SQLException, AuthorizeException {
        if (Objects.nonNull(afterImportActionList)) {
            for (AfterImportAction action : afterImportActionList) {
                action.applyTo(context, item, externalDataObject);
            }
        }
    }

    private static class ResearcherProfileSource {

        private final String[] path;

        ResearcherProfileSource(URI source) {

            this.path = source.getPath().split("/");
        }

        String id() {
            return path[path.length - 1];
        }

        String source() {
            return isDspace() ? "dspace" : path[path.length - 3];
        }

        //FIXME: improve way to distinguish between dspace and actually external objects
        private boolean isDspace() {
            return "server".equals(path[path.length - 5])
                && "api".equals(path[path.length - 4])
                && "core".equals(path[path.length - 3])
                && "items".equals(path[path.length - 2]);
        }
    }
}