/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import static java.lang.Math.toIntExact;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.dspace.app.orcid.OrcidQueue;
import org.dspace.app.orcid.service.OrcidQueueService;
import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.exception.RepositoryMethodNotImplementedException;
import org.dspace.app.rest.model.OrcidQueueRest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * This is the repository responsible of exposing Orcid queue resources.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
@Component(OrcidQueueRest.CATEGORY + "." + OrcidQueueRest.NAME)
public class OrcidQueueRestRepository extends DSpaceRestRepository<OrcidQueueRest, Integer> {

    @Autowired
    private OrcidQueueService orcidQueueService;

    @Override
    @PreAuthorize("hasPermission(#id, 'ORCID', 'READ')")
    public OrcidQueueRest findOne(Context context, Integer id) {
        OrcidQueue orcidQueue = null;
        try {
            orcidQueue = orcidQueueService.find(context, id);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (orcidQueue == null) {
            return null;
        }
        return converter.toRest(orcidQueue, utils.obtainProjection());
    }

    @Override
    public Page<OrcidQueueRest> findAll(Context context, Pageable pageable) {
        throw new RepositoryMethodNotImplementedException("No implementation found; Method not allowed!", "");
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'ORCID', 'DELETE')")
    protected void delete(Context context, Integer id) throws AuthorizeException {
        OrcidQueue orcidQueue = null;
        try {
            orcidQueue = orcidQueueService.find(context, id);
            if (orcidQueue == null) {
                throw new ResourceNotFoundException(
                    OrcidQueueRest.CATEGORY + "." + OrcidQueueRest.NAME + " with id: " + id + " not found");
            }
            orcidQueueService.deleteById(context, id);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to delete OrcidQueue with id = " + id, e);
        }
    }

    @SearchRestMethod(name = "findByOwner")
    @PreAuthorize("hasPermission(#ownerId, 'ORCID_SEARCH', 'READ')")
    public Page<OrcidQueueRest> findByOwnerId(@Parameter(value = "ownerId", required = true) String ownerId,
        Pageable pageable) {

        Context context = obtainContext();
        try {
            UUID id = UUID.fromString(ownerId);
            List<OrcidQueue> result = orcidQueueService.findByOwnerId(context, id, pageable.getPageSize(),
                toIntExact(pageable.getOffset()));
            long totalCount = orcidQueueService.countByOwnerId(context, id);
            return converter.toRestPage(result, pageable, totalCount, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    @Override
    public Class<OrcidQueueRest> getDomainClass() {
        return OrcidQueueRest.class;
    }

}