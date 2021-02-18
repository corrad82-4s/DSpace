/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import static org.dspace.app.rest.matcher.CrisLayoutSectionMatcher.withBrowseComponent;
import static org.dspace.app.rest.matcher.CrisLayoutSectionMatcher.withFacetComponent;
import static org.dspace.app.rest.matcher.CrisLayoutSectionMatcher.withIdAndBrowseComponent;
import static org.dspace.app.rest.matcher.CrisLayoutSectionMatcher.withIdAndCountersComponent;
import static org.dspace.app.rest.matcher.CrisLayoutSectionMatcher.withIdAndFacetComponent;
import static org.dspace.app.rest.matcher.CrisLayoutSectionMatcher.withIdAndSearchComponent;
import static org.dspace.app.rest.matcher.CrisLayoutSectionMatcher.withIdAndTextRowComponent;
import static org.dspace.app.rest.matcher.CrisLayoutSectionMatcher.withIdAndTopComponent;
import static org.dspace.app.rest.matcher.CrisLayoutSectionMatcher.withSearchComponent;
import static org.dspace.app.rest.matcher.CrisLayoutSectionMatcher.withTopComponent;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.junit.Test;

/**
 * Integration tests for {@link CrisLayoutSectionRestRepository}.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public class CrisLayoutSectionRestRepositoryIT extends AbstractControllerIntegrationTest {

    @Test
    public void testFindAll() throws Exception {

        String[] expectedBrowseNames = new String[] { "rotitle", "rodatecreated", "rodatemodified", "rodateissued"};

        getClient().perform(get("/api/layout/sections"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.sections", hasSize(6)))

            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndBrowseComponent("researchoutputs", 0, 0, "col-md-4", expectedBrowseNames))))
            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndSearchComponent("researchoutputs", 0, 1, "col-md-8", "researchoutputs"))))
            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndTopComponent("researchoutputs", 1, 0, "col-md-6",
                    "researchoutputs", "dc.date.accessioned", "desc"))))
            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndTopComponent("researchoutputs", 1, 1, "col-md-6",
                                    "researchoutputs", "dc.title", "asc"))))
            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndFacetComponent("researchoutputs", 2, 0, "col-md-12", "researchoutputs"))))

            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndBrowseComponent("researcherprofiles", 0, 0, "col-md-4",
                                                 "rpname", "rpdatecreated", "rpdatemodified"))))
            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndSearchComponent("researcherprofiles", 0, 1, "col-md-8", "person"))))

            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndBrowseComponent("fundings_and_projects", 0, 0, "col-md-4", "pftitle",
                                                 "pfdatecreated", "pfdatemodified", "pfdatestart", "pfdateend"))))
            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndSearchComponent("fundings_and_projects", 0, 1, "col-md-8", "project_funding"))))

            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndBrowseComponent("orgunits", 0, 0, "col-md-4", "ouname", "oudatecreated",
                                                 "oudatemodified"))))
            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndSearchComponent("orgunits", 0, 1, "col-md-8", "orgunit"))))
            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndFacetComponent("orgunits", 1, 0, "col-md-12", "orgunit"))))
            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndBrowseComponent("infrastructure", 0, 0, "col-md-4", "eqtitle", "eqdatecreated",
                                                 "eqdatemodified"))))
            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndSearchComponent("infrastructure", 0, 1, "col-md-8", "infrastructure"))))
            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndFacetComponent("infrastructure", 1, 0, "col-md-12", "infrastructure"))))
            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndTextRowComponent("directorios", 0, 0, "col-md-12 center", "image"))))
            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndTextRowComponent("directorios", 0, 1,
                    "col-md-12 h2 d-flex justify-content-center py-3","text-key"))))
            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndTextRowComponent("directorios", 0, 2, "col-md-12 d-flex justify-content-center py-2",
                    "text-key"))))
            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndSearchComponent("directorios", 1, 0, "col-md-12", "site"))))
            .andExpect(jsonPath("$._embedded.sections",
                hasItem(withIdAndCountersComponent("directorios", 2, 0, "col-md-12 py-4",
                    Arrays.asList("RESUME.rprofiles","RESUME.orgunits", "RESUME.researchoutputs",
                        "RESUME.project_funding", "RESUME.infrastructure")))));
    }

    @Test
    public void testFindOne() throws Exception {

        String[] expectedBrowseNames = new String[] { "rotitle", "rodatecreated", "rodatemodified", "rodateissued" };

        getClient().perform(get("/api/layout/sections/{id}", "researchoutputs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is("researchoutputs")))
            .andExpect(jsonPath("$", withBrowseComponent(0, 0, "col-md-4", expectedBrowseNames)))
            .andExpect(jsonPath("$", withSearchComponent(0, 1, "col-md-8", "researchoutputs")))
            .andExpect(jsonPath("$", withTopComponent(1, 0, "col-md-6", "researchoutputs",
                                                      "dc.date.accessioned", "desc")))
            .andExpect(jsonPath("$", withTopComponent(1, 1, "col-md-6", "researchoutputs", "dc.title", "asc")))
            .andExpect(jsonPath("$", withFacetComponent(2, 0, "col-md-12", "researchoutputs")));
    }

    @Test
    public void testFindOneWithUnknownSectionId() throws Exception {

        getClient().perform(get("/api/layout/sections/{id}", "unknown-section-id"))
            .andExpect(status().isNotFound());
    }
}
